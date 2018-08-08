package hluo.fun.playground.psi.server;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.airlift.bootstrap.Bootstrap;
import io.airlift.discovery.client.Announcer;
import io.airlift.discovery.client.DiscoveryModule;
import io.airlift.event.client.HttpEventModule;
import io.airlift.event.client.JsonEventModule;
import io.airlift.http.server.HttpServerModule;
import io.airlift.jaxrs.JaxrsModule;
import io.airlift.jmx.JmxHttpModule;
import io.airlift.jmx.JmxModule;
import io.airlift.json.JsonModule;
import io.airlift.log.LogJmxModule;
import io.airlift.log.Logger;
import io.airlift.node.NodeModule;
import org.weakref.jmx.guice.MBeanModule;

public class PsiServer
        implements Runnable

{
    public static void main(String[] args)
    {
        new PsiServer().run();
    }

    @Override
    public void run()
    {
        Logger log = Logger.get(PsiServer.class);

        ImmutableList.Builder<Module> modules = ImmutableList.builder();
        modules.add(
                new NodeModule(),
                new DiscoveryModule(),
                new HttpServerModule(),
                new JsonModule(),
                new JaxrsModule(true),
                new MBeanModule(),
                new JmxModule(),
                new JmxHttpModule(),
                new LogJmxModule(),
//                new TraceTokenModule(),
                new JsonEventModule(),
                new HttpEventModule(),
                new ServerMainModule());

        Bootstrap app = new Bootstrap(modules.build());

        try {
            Injector injector = app.strictConfig().initialize();

            injector.getInstance(Announcer.class).start();

            log.info("======== SERVER STARTED ========");
        }
        catch (Throwable e) {
            log.error(e);
            System.exit(1);
        }
    }
}
