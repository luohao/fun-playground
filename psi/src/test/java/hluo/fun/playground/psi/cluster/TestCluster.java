package hluo.fun.playground.psi.cluster;

import hluo.fun.playground.psi.testing.TestingPsiCluster;
import hluo.fun.playground.psi.testing.TestingPsiServer;
import org.testng.annotations.Test;

public class TestCluster
{
    @Test
    void testMaster() throws Exception {
        TestingPsiServer server = new TestingPsiServer();
    }

    @Test
    void testCluster() throws Exception {
        TestingPsiCluster.Builder builder = new TestingPsiCluster.Builder()
                .setSingleMasterProperty("http-server.http.port", "8080");
        builder.build();
    }
}
