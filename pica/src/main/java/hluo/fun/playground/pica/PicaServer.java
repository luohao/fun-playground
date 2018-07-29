package hluo.fun.playground.pica;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import io.airlift.bootstrap.Bootstrap;
import io.airlift.http.server.testing.TestingHttpServer;
import io.airlift.http.server.testing.TestingHttpServerModule;
import io.airlift.jaxrs.JaxrsModule;
import io.airlift.json.JsonModule;
import io.airlift.node.testing.TestingNodeModule;

import java.io.Closeable;
import java.net.URI;
import java.util.List;

import static com.google.common.base.Throwables.throwIfUnchecked;
import static io.airlift.jaxrs.JaxrsBinder.jaxrsBinder;

public class PicaServer
        implements Closeable
{
    private TestingHttpServer server;

    public PicaServer(boolean isMaster)
    {
        this.server = createPicaServer(isMaster);
    }

    public void start()
            throws Exception
    {
        server.start();
    }

    @Override
    public void close()
    {
        try {
            server.stop();
        }
        catch (Exception e) {
            throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    public URI getBaseUrl()
    {
        return server.getBaseUrl();
    }

    private static TestingHttpServer createPicaServer(final boolean isMaster)
    {
        try {

            return new Bootstrap(getServerModules(isMaster))
                    .strictConfig()
                    .doNotInitializeLogging()
                    .quiet()
                    .initialize()
                    .getInstance(TestingHttpServer.class);
        }
        catch (Exception e) {
            throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    private static List<Module> getServerModules(final boolean isMaster)
    {
        return ImmutableList.<Module>builder()
                .add(new TestingNodeModule())
                .add(new JaxrsModule(true))
                .add(new JsonModule())
                .add(new TestingHttpServerModule())
                .add(binder -> jaxrsBinder(binder).bindInstance(isMaster ? new PicaMasterResource() : new PicaWorkerResource()))
                .build();
    }
}
