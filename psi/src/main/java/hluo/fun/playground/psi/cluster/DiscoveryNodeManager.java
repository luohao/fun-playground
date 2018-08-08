package hluo.fun.playground.psi.cluster;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import hluo.fun.playground.psi.server.NodeState;
import io.airlift.discovery.client.ServiceDescriptor;
import io.airlift.discovery.client.ServiceSelector;
import io.airlift.discovery.client.ServiceType;
import io.airlift.http.client.HttpClient;
import io.airlift.log.Logger;
import io.airlift.node.NodeInfo;
import io.airlift.units.Duration;
import org.weakref.jmx.Managed;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Sets.difference;
import static hluo.fun.playground.psi.server.NodeState.ACTIVE;
import static hluo.fun.playground.psi.server.NodeState.INACTIVE;
import static io.airlift.concurrent.Threads.threadsNamed;
import static io.airlift.http.client.HttpUriBuilder.uriBuilderFrom;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

// Discovery service running in all nodes (both master and worker)
@ThreadSafe
public final class DiscoveryNodeManager
        implements NodeManager
{
    private static final Logger log = Logger.get(DiscoveryNodeManager.class);
    private static final Duration MAX_AGE = new Duration(5, TimeUnit.SECONDS);

    private final ServiceSelector serviceSelector;
    private final NodeInfo nodeInfo;
    private final NodeVersion expectedNodeVersion;
    private final ConcurrentHashMap<String, RemoteNodeState> nodeStates = new ConcurrentHashMap<>();
    private final HttpClient httpClient;
    private final ScheduledExecutorService nodeStateUpdateExecutor;

    @GuardedBy("this")
    private AllNodes allNodes;

    @GuardedBy("this")
    private long lastUpdateTimestamp;

    private final PsiNode currentNode;

    @GuardedBy("this")
    private Set<Node> masters;

    @Inject
    public DiscoveryNodeManager(
            @ServiceType("psi") ServiceSelector serviceSelector,
            NodeInfo nodeInfo,
            NodeVersion expectedNodeVersion,
            @ForNodeManager HttpClient httpClient)
    {
        this.serviceSelector = requireNonNull(serviceSelector, "serviceSelector is null");
        this.nodeInfo = requireNonNull(nodeInfo, "nodeInfo is null");
        this.expectedNodeVersion = requireNonNull(expectedNodeVersion, "expectedNodeVersion is null");
        this.httpClient = requireNonNull(httpClient, "httpClient is null");
        this.nodeStateUpdateExecutor = newSingleThreadScheduledExecutor(threadsNamed("node-state-poller-%s"));
        this.currentNode = refreshNodesInternal();
    }

    @PostConstruct
    public void startPollingNodeStates()
    {
        // poll worker states only on the masters
        if (getMasters().contains(currentNode)) {
            nodeStateUpdateExecutor.scheduleWithFixedDelay(() -> {
                ImmutableSet.Builder nodeSetBuilder = ImmutableSet.builder();
                AllNodes allNodes = getAllNodes();
                Set<Node> aliveNodes = nodeSetBuilder
                        .addAll(allNodes.getActiveNodes())
                        .build();

                ImmutableSet<String> aliveNodeIds = aliveNodes.stream()
                        .map(Node::getNodeIdentifier)
                        .collect(toImmutableSet());

                // Remove nodes that don't exist anymore
                // Make a copy to materialize the set difference
                Set<String> deadNodes = difference(nodeStates.keySet(), aliveNodeIds).immutableCopy();
                nodeStates.keySet().removeAll(deadNodes);

                // Add new nodes
                for (Node node : aliveNodes) {
                    nodeStates.putIfAbsent(node.getNodeIdentifier(),
                            new RemoteNodeState(httpClient, uriBuilderFrom(node.getHttpUri()).appendPath("/v1/info/state").build()));
                }

                // Schedule refresh
                nodeStates.values().forEach(RemoteNodeState::asyncRefresh);
            }, 1, 5, TimeUnit.SECONDS);
        }
    }

    @PreDestroy
    public void stop()
    {
        nodeStateUpdateExecutor.shutdownNow();
    }

    @Override
    public void refreshNodes()
    {
        refreshNodesInternal();
    }

    private synchronized PsiNode refreshNodesInternal()
    {
        lastUpdateTimestamp = System.nanoTime();

        // This is currently a blacklist.
        // TODO: make it a whitelist (a failure-detecting service selector) and maybe build in support for injecting this in airlift
        Set<ServiceDescriptor> services = serviceSelector.selectAllServices().stream()
                .collect(toImmutableSet());

        PsiNode currentNode = null;

        ImmutableSet.Builder<Node> activeNodesBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<Node> inactiveNodesBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<Node> shuttingDownNodesBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<Node> mastersBuilder = ImmutableSet.builder();

        for (ServiceDescriptor service : services) {
            URI uri = getHttpUri(service);
            NodeVersion nodeVersion = getNodeVersion(service);
            boolean master = isMaster(service);
            if (uri != null && nodeVersion != null) {
                PsiNode node = new PsiNode(service.getNodeId(), uri, nodeVersion, master);
                NodeState nodeState = getNodeState(node);

                // record current node
                if (node.getNodeIdentifier().equals(nodeInfo.getNodeId())) {
                    currentNode = node;
                    checkState(currentNode.getNodeVersion().equals(expectedNodeVersion), "INVARIANT: current node version (%s) should be equal to %s", currentNode.getNodeVersion(), expectedNodeVersion);
                }

                switch (nodeState) {
                    case ACTIVE:
                        activeNodesBuilder.add(node);
                        if (master) {
                            mastersBuilder.add(node);
                        }

                        break;
                    case INACTIVE:
                        inactiveNodesBuilder.add(node);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown node state " + nodeState);
                }
            }
        }

        if (allNodes != null) {
            // log node that are no longer active (but not shutting down)
            Sets.SetView<Node> missingNodes = difference(allNodes.getActiveNodes(), Sets.union(activeNodesBuilder.build(), shuttingDownNodesBuilder.build()));
            for (Node missingNode : missingNodes) {
                log.info("Previously active node is missing: %s (last seen at %s)", missingNode.getNodeIdentifier(), missingNode.getHostAndPort());
            }
        }

        allNodes = new AllNodes(activeNodesBuilder.build(), inactiveNodesBuilder.build());
        masters = mastersBuilder.build();

        checkState(currentNode != null, "INVARIANT: current node not returned from service selector");
        return currentNode;
    }

    private synchronized void refreshIfNecessary()
    {
        if (Duration.nanosSince(lastUpdateTimestamp).compareTo(MAX_AGE) > 0) {
            refreshNodesInternal();
        }
    }

    private NodeState getNodeState(PsiNode node)
    {
        if (expectedNodeVersion.equals(node.getNodeVersion())) {
            return ACTIVE;
        }
        else {
            return INACTIVE;
        }
    }

    @Override
    public synchronized AllNodes getAllNodes()
    {
        refreshIfNecessary();
        return allNodes;
    }

    @Managed
    public int getActiveNodeCount()
    {
        return getAllNodes().getActiveNodes().size();
    }

    @Managed
    public int getInactiveNodeCount()
    {
        return getAllNodes().getInactiveNodes().size();
    }

    @Override
    public Set<Node> getNodes(NodeState state)
    {
        switch (state) {
            case ACTIVE:
                return getAllNodes().getActiveNodes();
            case INACTIVE:
                return getAllNodes().getInactiveNodes();
            default:
                throw new IllegalArgumentException("Unknown node state " + state);
        }
    }

    @Override
    public Node getCurrentNode()
    {
        return currentNode;
    }

    @Override
    public synchronized Set<Node> getMasters()
    {
        refreshIfNecessary();
        return masters;
    }

    private URI getHttpUri(ServiceDescriptor descriptor)
    {
        String url = descriptor.getProperties().get("http");
        if (url != null) {
            try {
                return new URI(url);
            }
            catch (URISyntaxException ignored) {
            }
        }
        return null;
    }

    private static NodeVersion getNodeVersion(ServiceDescriptor descriptor)
    {
        String nodeVersion = descriptor.getProperties().get("node_version");
        return nodeVersion == null ? null : new NodeVersion(nodeVersion);
    }

    private static boolean isMaster(ServiceDescriptor service)
    {
        return Boolean.parseBoolean(service.getProperties().get("master"));
    }
}
