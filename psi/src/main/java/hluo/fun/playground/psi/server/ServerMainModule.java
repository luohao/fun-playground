package hluo.fun.playground.psi.server;

import com.google.inject.Binder;
import com.google.inject.Scopes;
import hluo.fun.playground.psi.test.TestNodeManager;
import io.airlift.configuration.AbstractConfigurationAwareModule;

import static io.airlift.discovery.client.DiscoveryBinder.discoveryBinder;

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

        // node manager
        discoveryBinder(binder).bindSelector("presto");
        binder.bind(TestNodeManager.class).in(Scopes.SINGLETON);

        discoveryBinder(binder).bindHttpAnnouncement("presto");
    }
}
