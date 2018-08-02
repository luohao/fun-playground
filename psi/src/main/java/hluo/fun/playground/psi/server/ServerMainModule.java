package hluo.fun.playground.psi.server;

import com.google.inject.Binder;
import com.google.inject.Scopes;
import hluo.fun.playground.psi.cluster.DiscoveryNodeManager;
import hluo.fun.playground.psi.cluster.ForNodeManager;
import hluo.fun.playground.psi.cluster.NodeManager;
import hluo.fun.playground.psi.cluster.NodeVersion;
import io.airlift.configuration.AbstractConfigurationAwareModule;
import io.airlift.units.Duration;

import static io.airlift.discovery.client.DiscoveryBinder.discoveryBinder;
import static io.airlift.http.client.HttpClientBinder.httpClientBinder;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.weakref.jmx.guice.ExportBinder.newExporter;

public class ServerMainModule
        extends AbstractConfigurationAwareModule
{
    @Override
    protected void setup(Binder binder)
    {
        ServerConfig serverConfig = buildConfigObject(ServerConfig.class);

        if (serverConfig.isGroupMaster()) {
            install(new MasterModule());
        }

        discoveryBinder(binder).bindSelector("psi");
        binder.bind(DiscoveryNodeManager.class).in(Scopes.SINGLETON);
        binder.bind(NodeManager.class).to(DiscoveryNodeManager.class).in(Scopes.SINGLETON);
        newExporter(binder).export(DiscoveryNodeManager.class).withGeneratedName();
        httpClientBinder(binder).bindHttpClient("node-manager", ForNodeManager.class)
                .withTracing()
                .withConfigDefaults(config -> {
                    config.setIdleTimeout(new Duration(30, SECONDS));
                    config.setRequestTimeout(new Duration(10, SECONDS));
                });

        NodeVersion nodeVersion = new NodeVersion(serverConfig.getPsiVersion());
        binder.bind(NodeVersion.class).toInstance(nodeVersion);

        // psi announcement
        discoveryBinder(binder).bindHttpAnnouncement("psi")
                .addProperty("node_version", nodeVersion.toString())
                .addProperty("master", String.valueOf(serverConfig.isGroupMaster()));

    }
}
