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
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static com.google.common.base.Throwables.throwIfUnchecked;
import static io.airlift.jaxrs.JaxrsBinder.jaxrsBinder;

public class PicaWorker
        implements Closeable
{
    private PicaWorkerResource resource;
    private TestingHttpServer server;

    public PicaWorker()
    {
        this.resource = new PicaWorkerResource();
        this.server = createPicaWorker(resource);
    }

    public void start()
            throws Exception
    {
        server.start();
    }

    @Override
    public void close()
            throws IOException
    {
        try {
            server.stop();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public URI getBaseUrl()
    {
        return server.getBaseUrl();
    }


    private static TestingHttpServer createPicaWorker(final PicaWorkerResource resource)
    {
        try {
            List<Module> modules = ImmutableList.<Module>builder()
                    .add(new TestingNodeModule())
                    .add(new JaxrsModule(true))
                    .add(new JsonModule())
                    .add(new TestingHttpServerModule())
                    .add(binder -> jaxrsBinder(binder).bindInstance(resource))
                    .build();

            return new Bootstrap(modules)
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
}
