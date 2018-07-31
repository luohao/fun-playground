package hluo.fun.playground.psi.server;

import com.google.inject.Binder;
import io.airlift.configuration.AbstractConfigurationAwareModule;
import io.airlift.discovery.server.EmbeddedDiscoveryModule;

import static io.airlift.discovery.client.DiscoveryBinder.discoveryBinder;
import static io.airlift.jaxrs.JaxrsBinder.jaxrsBinder;

public class MasterModule
        extends AbstractConfigurationAwareModule
{
    @Override
    protected void setup(Binder binder)
    {
        discoveryBinder(binder).bindHttpAnnouncement("psi-group-master");

        // job resource
        jaxrsBinder(binder).bind(JobResource.class);

        // discovery service
        install(new EmbeddedDiscoveryModule());
    }
}
