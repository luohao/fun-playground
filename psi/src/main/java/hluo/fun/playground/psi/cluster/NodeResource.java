package hluo.fun.playground.psi.cluster;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
        System.out.println("======== Get All Nodes ========");
        nodeManager.getAllNodes().getActiveNodes().stream()
                .forEach(x -> System.out.println(x.getHostAndPort()));
        return Response.ok().build();
    }
}
