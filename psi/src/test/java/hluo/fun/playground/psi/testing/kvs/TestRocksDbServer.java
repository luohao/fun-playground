package hluo.fun.playground.psi.testing.kvs;

import io.airlift.http.client.jetty.JettyHttpClient;
import io.airlift.testing.Closeables;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.IntStream;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

public class TestRocksDbServer
{
    private TestingKeyValueStore kvs;
    private KvsClient client;

    @BeforeMethod
    public void setup()
            throws Exception
    {
        kvs = new TestingKeyValueStore();
        client = new KvsClient(new JettyHttpClient(), kvs.getBaseUrl());
    }

    @SuppressWarnings("deprecation")
    @AfterMethod
    public void teardown()
    {
        Closeables.closeQuietly(kvs);
    }

    @Test
    void running()
    {
        while (true) {}
    }

    @Test
    void test()
    {
        // put a bunch of keys
        IntStream.range(0, 100)
                .forEach(x -> {
                    client.put(String.valueOf(x).getBytes(), String.valueOf(x).getBytes());
                });

        // check the value
        IntStream.range(0, 100)
                .forEach(x -> {
                    KvsResponse response = client.get(String.valueOf(x).getBytes());
                    assertTrue(response.isSuccessful());
                    assertFalse(response.getErrMessage().isPresent());
                    assertTrue(response.getReturnValue().isPresent());
                    assertEquals(new String(response.getReturnValue().get()), String.valueOf(x));
                });

        // delete odd keys
        IntStream.range(0, 100)
                .filter(x -> (x % 2 == 1))
                .forEach(x -> {
                    KvsResponse response = client.delete(String.valueOf(x).getBytes());
                    assertTrue(response.isSuccessful());
                    assertFalse(response.getErrMessage().isPresent());
                });

        // check the value
        IntStream.range(0, 100)
                .forEach(x -> {
                    KvsResponse response = client.get(String.valueOf(x).getBytes());
                    assertTrue(response.isSuccessful());
                    assertFalse(response.getErrMessage().isPresent());
                    if (x % 2 == 0) {
                        assertTrue(response.getReturnValue().isPresent());
                        assertEquals(new String(response.getReturnValue().get()), String.valueOf(x));
                    }
                    else {
                        assertFalse(response.getReturnValue().isPresent());
                    }
                });
    }
}
