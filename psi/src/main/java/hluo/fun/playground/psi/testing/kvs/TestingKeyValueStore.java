package hluo.fun.playground.psi.testing.kvs;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.google.inject.Module;
import hluo.fun.playground.psi.testing.TestUtils;
import hluo.fun.playground.psi.testing.TestingPsiCluster;
import io.airlift.bootstrap.Bootstrap;
import io.airlift.bootstrap.LifeCycleManager;
import io.airlift.http.server.testing.TestingHttpServer;
import io.airlift.http.server.testing.TestingHttpServerModule;
import io.airlift.jaxrs.JaxrsModule;
import io.airlift.json.JsonModule;
import io.airlift.log.Logger;
import io.airlift.node.testing.TestingNodeModule;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.base.Throwables.throwIfUnchecked;
import static com.google.common.io.MoreFiles.deleteRecursively;
import static com.google.common.io.RecursiveDeleteOption.ALLOW_INSECURE;
import static io.airlift.jaxrs.JaxrsBinder.jaxrsBinder;
import static java.nio.file.Files.isDirectory;

public class TestingKeyValueStore
        implements Closeable
{
    private static final Logger log = Logger.get(TestingKeyValueStore.class);
    private final Injector injector;
    private final Path baseDataDir;
    private final TestingHttpServer server;
    private final LifeCycleManager lifeCycleManager;
    private final KeyValueStore kvs;

    public TestingKeyValueStore()
            throws Exception
    {
        baseDataDir = Files.createTempDirectory("KvsTest");

        List modules = ImmutableList.<Module>builder()
                .add(new TestingNodeModule())
                .add(new JaxrsModule(true))
                .add(new JsonModule())
                .add(new TestingHttpServerModule(TestUtils.findUnusedPort()))
                .add(binder -> {
                    jaxrsBinder(binder).bind(KeyValueStoreResource.class);
                    KeyValueStore kvs = new RocksDbServer(baseDataDir.toString());
                    binder.bind(KeyValueStore.class).toInstance(kvs);
                })
                .build();

        Bootstrap app = new Bootstrap(modules);
        injector = app
                .strictConfig()
                .doNotInitializeLogging()
                .quiet()
                .initialize();

        server = injector.getInstance(TestingHttpServer.class);
        lifeCycleManager = injector.getInstance(LifeCycleManager.class);
        kvs = injector.getInstance(KeyValueStore.class);

        log.info("====== Server Started ======");
    }

    @Override
    public void close()
            throws IOException
    {
        try {
            if (lifeCycleManager != null) {
                lifeCycleManager.stop();
            }
            kvs.close();
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
}
