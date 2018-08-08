package hluo.fun.playground.psi.testing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.HostAndPort;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import hluo.fun.playground.psi.cluster.AllNodes;
import hluo.fun.playground.psi.cluster.NodeManager;
import hluo.fun.playground.psi.execution.TaskManager;
import hluo.fun.playground.psi.server.ServerMainModule;
import io.airlift.bootstrap.Bootstrap;
import io.airlift.bootstrap.LifeCycleManager;
import io.airlift.discovery.client.Announcer;
import io.airlift.discovery.client.DiscoveryModule;
import io.airlift.discovery.client.ServiceAnnouncement;
import io.airlift.discovery.client.ServiceSelectorManager;
import io.airlift.discovery.client.testing.TestingDiscoveryModule;
import io.airlift.event.client.EventModule;
import io.airlift.http.server.testing.TestingHttpServer;
import io.airlift.http.server.testing.TestingHttpServerModule;
import io.airlift.jaxrs.JaxrsModule;
import io.airlift.jmx.testing.TestingJmxModule;
import io.airlift.json.JsonModule;
import io.airlift.node.testing.TestingNodeModule;
import io.airlift.tracetoken.TraceTokenModule;
import org.weakref.jmx.guice.MBeanModule;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Throwables.throwIfUnchecked;
import static com.google.common.io.MoreFiles.deleteRecursively;
import static com.google.common.io.RecursiveDeleteOption.ALLOW_INSECURE;
import static java.lang.Integer.parseInt;
import static java.nio.file.Files.isDirectory;
import static java.util.Objects.requireNonNull;

public class TestingPsiServer
        implements Closeable
{
    private final Injector injector;
    private final Path baseDataDir;
    private final LifeCycleManager lifeCycleManager;
    private final TestingHttpServer server;
    private final NodeManager nodeManager;
    private final TaskManager taskManager;
    private final ServiceSelectorManager serviceSelectorManager;
    private final Announcer announcer;
    private final boolean master;

    // create a master server
    public TestingPsiServer()
            throws Exception
    {
        this(true, ImmutableList.of());
    }

    public TestingPsiServer(boolean master, List<Module> additionalModules)
            throws Exception
    {
        this(master, ImmutableMap.of(), null, null, ImmutableList.of());
    }

    public TestingPsiServer(boolean master,
            Map<String, String> properties,
            String environment,
            URI discoveryUri,
            List<Module> additionalModules)
            throws Exception
    {
        this.master = master;
        baseDataDir = Files.createTempDirectory("PsiTest");

        properties = new HashMap<>(properties);
        String masterPort = properties.remove("http-server.http.port");
        if (masterPort == null) {
            masterPort = "0";
        }

        ImmutableMap.Builder<String, String> serverProperties = ImmutableMap.<String, String>builder()
                .putAll(properties)
                .put("master", String.valueOf(master))
                .put("psi.version", "testversion");

        ImmutableList.Builder<Module> modules = ImmutableList.<Module>builder()
                .add(new TestingNodeModule(Optional.ofNullable(environment)))
                .add(new TestingHttpServerModule(parseInt(master ? masterPort : "0")))
                .add(new JsonModule())
                .add(new JaxrsModule(true))
                .add(new MBeanModule())
                .add(new TestingJmxModule())
                .add(new EventModule())
                .add(new TraceTokenModule())
                .add(new ServerMainModule());

        if (discoveryUri != null) {
            requireNonNull(environment, "environment required when discoveryUri is present");
            serverProperties.put("discovery.uri", discoveryUri.toString());
            modules.add(new DiscoveryModule());
        }
        else {
            modules.add(new TestingDiscoveryModule());
        }

        modules.addAll(additionalModules);

        Bootstrap app = new Bootstrap(modules.build());

        Map<String, String> optionalProperties = new HashMap<>();
        if (environment != null) {
            optionalProperties.put("node.environment", environment);
        }

        injector = app
                .strictConfig()
                .doNotInitializeLogging()
                .setRequiredConfigurationProperties(serverProperties.build())
                .setOptionalConfigurationProperties(optionalProperties)
                .quiet()
                .initialize();

        injector.getInstance(Announcer.class).start();
        lifeCycleManager = injector.getInstance(LifeCycleManager.class);
        server = injector.getInstance(TestingHttpServer.class);
        nodeManager = injector.getInstance(NodeManager.class);
        taskManager = injector.getInstance(TaskManager.class);
        serviceSelectorManager = injector.getInstance(ServiceSelectorManager.class);
        announcer = injector.getInstance(Announcer.class);
        announcer.forceAnnounce();

        refreshNodes();
    }

    @Override
    public void close()
            throws IOException
    {
        try {
            if (lifeCycleManager != null) {
                lifeCycleManager.stop();
            }
        }
        catch (Exception e) {
            throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
        finally {
            if (isDirectory(baseDataDir)) {
                deleteRecursively(baseDataDir, ALLOW_INSECURE);
            }
        }
    }

    public URI getBaseUrl() { return server.getBaseUrl(); }

    public URI resolve(String path)
    {
        return server.getBaseUrl().resolve(path);
    }

    public HostAndPort getAddress()
    {
        return HostAndPort.fromParts(getBaseUrl().getHost(), getBaseUrl().getPort());
    }

    public HostAndPort getHttpsAddress()
    {
        URI httpsUri = server.getHttpServerInfo().getHttpsUri();
        return HostAndPort.fromParts(httpsUri.getHost(), httpsUri.getPort());
    }

    public boolean isMaster()
    {
        return master;
    }

    public <T> T getInstance(Key<T> key)
    {
        return injector.getInstance(key);
    }

    private static ServiceAnnouncement getPsiAnnouncement(Set<ServiceAnnouncement> announcements)
    {
        for (ServiceAnnouncement announcement : announcements) {
            if (announcement.getType().equals("psi")) {
                return announcement;
            }
        }
        throw new RuntimeException("Psi announcement not found: " + announcements);
    }

    public final AllNodes refreshNodes()
    {
        serviceSelectorManager.forceRefresh();
        nodeManager.refreshNodes();
        return nodeManager.getAllNodes();
    }
}
