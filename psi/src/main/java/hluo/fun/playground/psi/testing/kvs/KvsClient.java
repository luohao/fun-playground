package hluo.fun.playground.psi.testing.kvs;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import io.airlift.http.client.HttpClient;
import io.airlift.http.client.JsonResponseHandler;
import io.airlift.http.client.Request;
import io.airlift.json.JsonCodec;

import java.net.URI;
import java.util.Optional;

import static io.airlift.http.client.HttpUriBuilder.uriBuilderFrom;
import static io.airlift.http.client.JsonBodyGenerator.jsonBodyGenerator;
import static io.airlift.http.client.JsonResponseHandler.createJsonResponseHandler;
import static io.airlift.http.client.Request.Builder.preparePost;
import static io.airlift.json.JsonCodec.jsonCodec;
import static java.util.Objects.requireNonNull;

public class KvsClient
{
    private final HttpClient httpClient;
    private final URI serverUrl;
    private final JsonResponseHandler<KvsResponse> responseHandler;
    private final JsonCodec<KvsRequest> kvsRequestCodec;

    public KvsClient(HttpClient httpClient, URI serverUrl)
    {
        this.httpClient = requireNonNull(httpClient, "httpClient is null");
        this.serverUrl = requireNonNull(serverUrl, "serverUrl is null");
        this.responseHandler = createJsonResponseHandler(jsonCodec(KvsResponse.class));
        this.kvsRequestCodec = jsonCodec(KvsRequest.class);
    }

    public KvsResponse get(byte[] key)
    {
        KvsRequest kvsRequest = new KvsRequest(KvsOperation.GET, key, Optional.empty());

        Request httpRequest = preparePost()
                .setUri(uriBuilderFrom(serverUrl).replacePath("/v1/kvs").build())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
                .setBodyGenerator(jsonBodyGenerator(kvsRequestCodec, kvsRequest))
                .build();

        return httpClient.execute(httpRequest, responseHandler);
    }

    public KvsResponse put(byte[] key, byte[] value)
    {
        KvsRequest kvsRequest = new KvsRequest(KvsOperation.PUT, key, Optional.of(value));

        Request httpRequest = preparePost()
                .setUri(uriBuilderFrom(serverUrl).replacePath("/v1/kvs").build())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
                .setBodyGenerator(jsonBodyGenerator(kvsRequestCodec, kvsRequest))
                .build();

        return httpClient.execute(httpRequest, responseHandler);
    }

    public KvsResponse delete(byte[] key)
    {
        KvsRequest kvsRequest = new KvsRequest(KvsOperation.DELETE, key, Optional.empty());

        Request httpRequest = preparePost()
                .setUri(uriBuilderFrom(serverUrl).replacePath("/v1/kvs").build())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
                .setBodyGenerator(jsonBodyGenerator(kvsRequestCodec, kvsRequest))
                .build();

        return httpClient.execute(httpRequest, responseHandler);
    }
}
