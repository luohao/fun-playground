package hluo.fun.playground.psi.server;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Scopes;
import hluo.fun.playground.psi.cluster.DiscoveryNodeManager;
import hluo.fun.playground.psi.cluster.ForNodeManager;
import hluo.fun.playground.psi.cluster.NodeManager;
import hluo.fun.playground.psi.cluster.NodeResource;
import hluo.fun.playground.psi.cluster.NodeVersion;
import hluo.fun.playground.psi.compiler.ClassInfo;
import hluo.fun.playground.psi.execution.ForScheduler;
import hluo.fun.playground.psi.execution.SimpleTaskExecutor;
import hluo.fun.playground.psi.execution.TaskExecutor;
import hluo.fun.playground.psi.execution.TaskInfo;
import hluo.fun.playground.psi.execution.TaskManager;
import hluo.fun.playground.psi.execution.TaskResource;
import io.airlift.configuration.AbstractConfigurationAwareModule;
import io.airlift.discovery.server.EmbeddedDiscoveryModule;
import io.airlift.units.Duration;

import static io.airlift.configuration.ConditionalModule.installModuleIf;
import static io.airlift.discovery.client.DiscoveryBinder.discoveryBinder;
import static io.airlift.http.client.HttpClientBinder.httpClientBinder;
import static io.airlift.jaxrs.JaxrsBinder.jaxrsBinder;
import static io.airlift.json.JsonCodecBinder.jsonCodecBinder;
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
        else {
            // for workers only
            jaxrsBinder(binder).bind(TaskResource.class);
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

        // httpClient
        httpClientBinder(binder).bindHttpClient("taskClassInfo", ForScheduler.class);

        // shutdown handler
        binder.bind(ShutdownHandler.class).in(Scopes.SINGLETON);

        jaxrsBinder(binder).bind(NodeResource.class);
        jaxrsBinder(binder).bind(ServerInfoResource.class);

        // task executor
        binder.bind(SimpleTaskExecutor.class).in(Scopes.SINGLETON);
        binder.bind(TaskExecutor.class).to(Key.get(SimpleTaskExecutor.class));
        newExporter(binder).export(TaskExecutor.class).withGeneratedName();
        binder.bind(TaskManager.class).in(Scopes.SINGLETON);

        // json codec
        jsonCodecBinder(binder).bindJsonCodec(TaskUpdateRequest.class);
        jsonCodecBinder(binder).bindJsonCodec(TaskInfo.class);
        jsonCodecBinder(binder).bindJsonCodec(ClassInfo.class);
    }
}
