package hluo.fun.playground.psi.cluster;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import hluo.fun.playground.psi.server.NodeState;
import io.airlift.http.client.FullJsonResponseHandler;
import io.airlift.http.client.HttpClient;
import io.airlift.http.client.Request;
import io.airlift.log.Logger;
import io.airlift.units.Duration;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.net.MediaType.JSON_UTF_8;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static io.airlift.http.client.FullJsonResponseHandler.createFullJsonResponseHandler;
import static io.airlift.http.client.HttpStatus.OK;
import static io.airlift.http.client.Request.Builder.prepareGet;
import static io.airlift.json.JsonCodec.jsonCodec;
import static io.airlift.units.Duration.nanosSince;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;

@ThreadSafe
public class RemoteNodeState
{
    private static final Logger log = Logger.get(RemoteNodeState.class);

    private final HttpClient httpClient;
    private final URI stateInfoUri;

    private final AtomicReference<Optional<NodeState>> nodeState = new AtomicReference<>(empty());
    private final AtomicReference<Future<?>> future = new AtomicReference<>();
    private final AtomicLong lastUpdateNanos = new AtomicLong();
    private final AtomicLong lastWarningLogged = new AtomicLong();

    public RemoteNodeState(HttpClient httpClient, URI stateInfoUri)
    {
        this.httpClient = requireNonNull(httpClient, "httpClient is null");
        this.stateInfoUri = requireNonNull(stateInfoUri, "stateInfoUri is null");
    }

    public Optional<NodeState> getNodeState()
    {
        return nodeState.get();
    }

    public synchronized void asyncRefresh()
    {
        Duration sinceUpdate = nanosSince(lastUpdateNanos.get());
        if (nanosSince(lastWarningLogged.get()).toMillis() > 1_000 &&
                sinceUpdate.toMillis() > 10_000 &&
                future.get() != null) {
            log.warn("Node state update request to %s has not returned in %s", stateInfoUri, sinceUpdate.toString(SECONDS));
            lastWarningLogged.set(System.nanoTime());
        }
        if (sinceUpdate.toMillis() > 1_000 && future.get() == null) {
            Request request = prepareGet()
                    .setUri(stateInfoUri)
                    .setHeader(CONTENT_TYPE, JSON_UTF_8.toString())
                    .build();
            HttpClient.HttpResponseFuture<FullJsonResponseHandler.JsonResponse<NodeState>> responseFuture = httpClient.executeAsync(request, createFullJsonResponseHandler(jsonCodec(NodeState.class)));
            future.compareAndSet(null, responseFuture);

            Futures.addCallback(responseFuture, new FutureCallback<FullJsonResponseHandler.JsonResponse<NodeState>>()
            {
                @Override
                public void onSuccess(@Nullable FullJsonResponseHandler.JsonResponse<NodeState> result)
                {
                    lastUpdateNanos.set(System.nanoTime());
                    future.compareAndSet(responseFuture, null);
                    if (result != null) {
                        if (result.hasValue()) {
                            nodeState.set(ofNullable(result.getValue()));
                        }
                        if (result.getStatusCode() != OK.code()) {
                            log.warn("Error fetching node state from %s returned status %d: %s", stateInfoUri, result.getStatusCode(), result.getStatusMessage());
                            return;
                        }
                    }
                }

                @Override
                public void onFailure(Throwable t)
                {
                    log.warn("Error fetching node state from %s: %s", stateInfoUri, t.getMessage());
                    lastUpdateNanos.set(System.nanoTime());
                    future.compareAndSet(responseFuture, null);
                }
            }, directExecutor());
        }
    }
}
