package hluo.fun.playground.psi.cluster;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

@Path("/v1/node")
public class NodeResource
{
    private final NodeManager nodeManager;

    @Inject
    public NodeResource(NodeManager nodeManager)
    {
        this.nodeManager = nodeManager;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllNodes()
    {
        List<URI> masters = nodeManager.getMasters().stream()
                .map(x -> x.getHttpUri())
                .collect(toImmutableList());
        List<URI> workers = nodeManager.getAllNodes().getActiveNodes().stream()
                .filter(x -> !nodeManager.getMasters().contains(x))
                .map(x -> x.getHttpUri())
                .collect(toImmutableList());

        return Response.ok().entity(new ClusterInfo(masters, workers)).build();
    }
}
