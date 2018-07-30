package hluo.fun.playground.pica;

import com.facebook.presto.client.JsonResponse;
import com.facebook.presto.client.OkHttpUtil;
import io.airlift.json.JsonCodec;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.net.URI;
import java.util.List;

import static io.airlift.json.JsonCodec.jsonCodec;
import static io.airlift.json.JsonCodec.listJsonCodec;

public class PicaClient
{
    private static final MediaType MEDIA_TYPE_TEXT = MediaType.parse("text/plain; charset=utf-8");

    private static final JsonCodec<FunctionId> FUNCTION_ID_JSON_CODEC = jsonCodec(FunctionId.class);
    private static final JsonCodec<List<FunctionId>> FUNCTION_ID_LIST_JSON_CODEC = listJsonCodec(FunctionId.class);
    private final URI masterUrl;
    // TODO: consider using io.airlift.http-client
    private final OkHttpClient httpClient;

    public PicaClient(URI masterUrl)
    {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        this.httpClient = builder.build();
        this.masterUrl = masterUrl;
    }

    public PicaClient(OkHttpClient httpClient, URI masterUrl)
    {
        this.httpClient = httpClient;
        this.masterUrl = masterUrl;
    }

    // submit function to Pica master at baseUrl
    public FunctionId submitFunction(String className, String functionSourceCode)
    {
        HttpUrl url = HttpUrl.get(masterUrl);
        if (url == null) {
            return null;
        }
        url = url.newBuilder().encodedPath("/v1/functions").build();

        // TODO: create a class for all HEADERs
        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-Pica-Class-Name", className)
                .post(RequestBody.create(MEDIA_TYPE_TEXT, functionSourceCode))
                .build();

        JsonResponse<FunctionId> response = JsonResponse.execute(FUNCTION_ID_JSON_CODEC, httpClient, request);
        return response.getValue();
    }

    // submit function to Pica master at baseUrl
    public List<FunctionId> listFunction()
    {
        HttpUrl url = HttpUrl.get(masterUrl);
        if (url == null) {
            return null;
        }
        url = url.newBuilder().encodedPath("/v1/functions").build();

        // TODO: create a class for all HEADERs
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        JsonResponse<List<FunctionId>> response = JsonResponse.execute(FUNCTION_ID_LIST_JSON_CODEC, httpClient, request);
        return response.getValue();
    }

    // submit function to Pica master at baseUrl
    public void stopFunction(FunctionId functionId)
    {
        HttpUrl url = HttpUrl.get(masterUrl);
        if (url == null) {
            return;
        }
        url = url.newBuilder().encodedPath("/v1/functions/" + functionId.getId()).build();

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        httpClient.newCall(request).enqueue(new OkHttpUtil.NullCallback());
    }
}
