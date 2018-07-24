package hluo.fun.playground.pica;

import io.airlift.http.client.HttpClient;
import io.airlift.http.client.HttpClientConfig;
import io.airlift.http.client.Request;
import io.airlift.http.client.StatusResponseHandler;
import io.airlift.http.client.jetty.JettyHttpClient;
import io.airlift.units.Duration;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;

import java.net.URI;

import static io.airlift.http.client.StatusResponseHandler.createStatusResponseHandler;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.testng.Assert.assertEquals;

public class TestPicaWorker
{
    @Test
    public void testRequest()
            throws Exception
    {
        PicaWorker worker = new PicaWorker();
        try {
            worker.start();

//            while (true) {}
            try (HttpClient client = new JettyHttpClient(new HttpClientConfig().setConnectTimeout(new Duration(1, SECONDS)))) {
                {
                    StatusResponseHandler.StatusResponse response = client.execute(buildRequest("GET", worker.getBaseUrl(), "/list/functions"), createStatusResponseHandler());
                    assertEquals(response.getStatusCode(), HttpServletResponse.SC_OK);
                }
                {
                    StatusResponseHandler.StatusResponse response = client.execute(buildRequest("POST", worker.getBaseUrl(), "/start"), createStatusResponseHandler());
                    assertEquals(response.getStatusCode(), HttpServletResponse.SC_OK);
                }
                {
                    StatusResponseHandler.StatusResponse response = client.execute(buildRequest("DELETE", worker.getBaseUrl(), "/stop/1"), createStatusResponseHandler());
                    assertEquals(response.getStatusCode(), HttpServletResponse.SC_OK);
                }
            }
        }
        finally {
            worker.close();
        }
    }

    Request buildRequest(String type, URI baseUri, String path)
    {
        return Request.builder().setUri(baseUri.resolve(format(path))).setMethod(type).build();
    }
}
