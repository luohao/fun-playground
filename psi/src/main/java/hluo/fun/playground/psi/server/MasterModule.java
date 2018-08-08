package hluo.fun.playground.psi.server;

import com.google.inject.Binder;
import com.google.inject.Scopes;
import hluo.fun.playground.psi.cluster.NodeResource;
import io.airlift.configuration.AbstractConfigurationAwareModule;
import io.airlift.discovery.server.EmbeddedDiscoveryModule;
//import io.airlift.discovery.server.EmbeddedDiscoveryModule;

import static io.airlift.configuration.ConditionalModule.installModuleIf;
import static io.airlift.discovery.client.DiscoveryBinder.discoveryBinder;
import static io.airlift.jaxrs.JaxrsBinder.jaxrsBinder;

public class MasterModule
        extends AbstractConfigurationAwareModule
{
    @Override
    protected void setup(Binder binder)
    {
        // psi master announcement
        discoveryBinder(binder).bindHttpAnnouncement("psi-master");

        // discovery server
        // FIXME: deploy discover server separately
        install(installModuleIf(EmbeddedDiscoveryConfig.class, EmbeddedDiscoveryConfig::isEnabled, new EmbeddedDiscoveryModule()));

        // job resource
        jaxrsBinder(binder).bind(JobResource.class);

        // job manager
        binder.bind(JobManager.class).in(Scopes.SINGLETON);
    }
}
