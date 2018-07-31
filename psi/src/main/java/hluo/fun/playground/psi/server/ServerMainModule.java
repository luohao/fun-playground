package hluo.fun.playground.psi.server;

import com.google.inject.Binder;
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
        discoveryBinder(binder).bindSelector("psi");

        discoveryBinder(binder).bindHttpAnnouncement("psi");

    }
}
