package hluo.fun.playground.pica;

import com.google.common.net.MediaType;
import hluo.fun.playground.pica.compiler.ClassInfo;
import io.airlift.http.client.HttpClient;
import io.airlift.http.client.HttpClientConfig;
import io.airlift.http.client.HttpUriBuilder;
import io.airlift.http.client.Request;
import io.airlift.http.client.jetty.JettyHttpClient;
import io.airlift.units.Duration;

import javax.ws.rs.core.HttpHeaders;

import java.net.URI;

import static io.airlift.http.client.HttpUriBuilder.uriBuilderFrom;
import static io.airlift.http.client.JsonResponseHandler.createJsonResponseHandler;
import static io.airlift.http.client.Request.Builder.prepareGet;
import static io.airlift.json.JsonCodec.jsonCodec;
import static java.util.concurrent.TimeUnit.SECONDS;

public final class ClientUtils
{
    public static ClassInfo getClassInfo(URI url, FunctionId functionId)
    {
        ClassInfo classInfo = null;
        try (HttpClient client = new JettyHttpClient(new HttpClientConfig().setConnectTimeout(new Duration(1, SECONDS)))) {
            HttpUriBuilder uriBuilder = uriBuilderFrom(url).appendPath("/v1/functions/" + functionId.getId());
            Request request = prepareGet()
                    .setUri(uriBuilder.build())
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
                    .build();
            classInfo =  client.execute(request, createJsonResponseHandler(jsonCodec(ClassInfo.class)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classInfo;
    }
}
