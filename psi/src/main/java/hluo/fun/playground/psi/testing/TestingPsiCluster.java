package hluo.fun.playground.psi.testing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Closer;
import hluo.fun.playground.psi.cluster.PsiCluster;
import io.airlift.discovery.server.testing.TestingDiscoveryServer;
import io.airlift.log.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Throwables.throwIfUnchecked;
import static io.airlift.units.Duration.nanosSince;

public class TestingPsiCluster
        implements PsiCluster

{
    private static final Logger log = Logger.get(TestingPsiCluster.class);
    private static final String ENVIRONMENT = "testing";
    private final TestingDiscoveryServer discoveryServer;
    private final TestingPsiServer master;
    private final List<TestingPsiServer> servers;

    private final Closer closer = Closer.create();

    private TestingPsiCluster(
            int nodeCount,
            Map<String, String> extraProperties,
            Map<String, String> masterProperties,
            String environment)
            throws Exception
    {
        // create the cluster
        try {
            long start = System.nanoTime();
            // create discovery server
            discoveryServer = new TestingDiscoveryServer(environment);
            closer.register(() -> closeUnchecked(discoveryServer));
            log.info("Created TestingDiscoveryServer in %s", nanosSince(start).convertToMostSuccinctTimeUnit());

            ImmutableList.Builder<TestingPsiServer> servers = ImmutableList.builder();
            // create workers
            for (int i = 1; i < nodeCount; i++) {
                TestingPsiServer worker = closer.register(createTestingPrestoServer(discoveryServer.getBaseUrl(), false, extraProperties, environment));
                servers.add(worker);
            }

            // create master
            Map<String, String> extraMasterProperties = new HashMap<>();
            extraMasterProperties.putAll(extraProperties);
            extraMasterProperties.putAll(masterProperties);
            master = closer.register(createTestingPrestoServer(discoveryServer.getBaseUrl(), true, extraMasterProperties, environment));
            servers.add(master);

            this.servers = servers.build();
        }
        catch (Exception e) {
            try {
                throw closer.rethrow(e, Exception.class);
            }
            finally {
                closer.close();
            }
        }

        log.info("====== Server Started ======");
    }

    private static TestingPsiServer createTestingPrestoServer(URI discoveryUri, boolean master, Map<String, String> extraProperties, String environment)
            throws Exception
    {
        long start = System.nanoTime();
        TestingPsiServer server = new TestingPsiServer(master, extraProperties, environment, discoveryUri, ImmutableList.of());
        log.info("Created TestingPsiServer in %s", nanosSince(start).convertToMostSuccinctTimeUnit());

        return server;
    }

    private static void closeUnchecked(AutoCloseable closeable)
    {
        try {
            closeable.close();
        }
        catch (Exception e) {
            throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close()
    {
        try {
            closer.close();
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public int getNodeCount()
    {
        return servers.size();
    }

    public static class Builder
    {
        private int nodeCount = 4;
        private Map<String, String> extraProperties = ImmutableMap.of();
        private Map<String, String> masterProperties = ImmutableMap.of();
        private String environment = ENVIRONMENT;

        public Builder setNodeCount(int nodeCount)
        {
            this.nodeCount = nodeCount;
            return this;
        }

        public Builder setExtraProperties(Map<String, String> extraProperties)
        {
            this.extraProperties = extraProperties;
            return this;
        }

        public Builder setMasterProperties(Map<String, String> masterProperties)
        {
            this.masterProperties = masterProperties;
            return this;
        }

        public Builder setSingleMasterProperty(String key, String value)
        {
            return setMasterProperties(ImmutableMap.of(key, value));
        }

        public Builder setEnvironment(String environment)
        {
            this.environment = environment;
            return this;
        }

        public TestingPsiCluster build()
            throws Exception
        {
            return new TestingPsiCluster(nodeCount, extraProperties, masterProperties, environment);
        }

    }
}
