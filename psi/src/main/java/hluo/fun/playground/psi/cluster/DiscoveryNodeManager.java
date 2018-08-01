package hluo.fun.playground.psi.cluster;

import hluo.fun.playground.psi.server.NodeState;
import io.airlift.discovery.client.ServiceDescriptor;
import io.airlift.discovery.client.ServiceSelector;
import io.airlift.http.client.HttpClient;
import io.airlift.log.Logger;
import io.airlift.node.NodeInfo;
import io.airlift.units.Duration;
import org.weakref.jmx.Managed;

import javax.annotation.concurrent.GuardedBy;
import javax.inject.Inject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Discovery service running in all nodes (both master and worker)
public class DiscoveryNodeManager
        implements NodeManager
{
    private static final Logger log = Logger.get(DiscoveryNodeManager.class);
    private static final Duration MAX_AGE = new Duration(5, TimeUnit.SECONDS);

    private final ServiceSelector serviceSelector;
    private final NodeInfo nodeInfo;
    private final ConcurrentHashMap<String, RemoteNodeState> nodeStates = new ConcurrentHashMap<>();
    private final HttpClient httpClient;
    private final ScheduledExecutorService nodeStateUpdateExecutor;

    @GuardedBy("this")
    private AllNodes allNodes;

    @GuardedBy("this")
    private long lastUpdateTimestamp;

    @Inject
    public DiscoveryNodeManager(ServiceSelector serviceSelector, NodeInfo nodeInfo, HttpClient httpClient, ScheduledExecutorService nodeStateUpdateExecutor)
    {
        this.serviceSelector = serviceSelector;
        this.nodeInfo = nodeInfo;
        this.httpClient = httpClient;
        this.nodeStateUpdateExecutor = nodeStateUpdateExecutor;
    }

//    @PostConstruct
//    public void startPollingNodeStates()
//    {
//        // poll worker states only on the coordinators
//        if (isMaster(service)) {
//            nodeStateUpdateExecutor.scheduleWithFixedDelay(() -> {
//                ImmutableSet.Builder nodeSetBuilder = ImmutableSet.builder();
//                AllNodes allNodes = getAllNodes();
//                Set<Node> aliveNodes = nodeSetBuilder
//                        .addAll(allNodes.getActiveNodes())
//                        .build();
//
//                ImmutableSet<String> aliveNodeIds = aliveNodes.stream()
//                        .map(Node::getNodeIdentifier)
//                        .collect(toImmutableSet());
//
//                // Remove nodes that don't exist anymore
//                // Make a copy to materialize the set difference
//                Set<String> deadNodes = difference(nodeStates.keySet(), aliveNodeIds).immutableCopy();
//                nodeStates.keySet().removeAll(deadNodes);
//
//                // Add new nodes
//                for (Node node : aliveNodes) {
//                    nodeStates.putIfAbsent(node.getNodeIdentifier(),
//                            new RemoteNodeState(httpClient, uriBuilderFrom(node.getHttpUri()).appendPath("/v1/info/state").build()));
//                }
//
//                // Schedule refresh
//                nodeStates.values().forEach(RemoteNodeState::asyncRefresh);
//            }, 1, 5, TimeUnit.SECONDS);
//        }
//    }
//
    private synchronized void refreshIfNecessary()
    {
//        if (Duration.nanosSince(lastUpdateTimestamp).compareTo(MAX_AGE) > 0) {
//            refreshNodesInternal();
//        }
    }
//
//    private NodeState getNodeState(PrestoNode node)
//    {
//        if (expectedNodeVersion.equals(node.getNodeVersion())) {
//            return ACTIVE;
//        }
//        else {
//            return INACTIVE;
//        }
//    }

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
        return null;
    }

    @Override
    public Node getCurrentNode()
    {
        return null;
    }

    @Override
    public void refreshNodes()
    {

    }

    public synchronized Set<Node> getMaster()
    {
        refreshIfNecessary();
        return null;
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

    private static boolean isMaster(ServiceDescriptor service)
    {
        return Boolean.parseBoolean(service.getProperties().get("psi-group-master"));
    }
}
