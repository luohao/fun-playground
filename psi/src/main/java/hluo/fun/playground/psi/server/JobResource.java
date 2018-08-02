package hluo.fun.playground.psi.server;

import hluo.fun.playground.psi.cluster.NodeManager;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1/job/")
public class JobResource
{
    private final NodeManager nodeManager;

    @Inject
    public JobResource(NodeManager nodeManager)
    {
        this.nodeManager = nodeManager;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFunctionList()
    {
        System.out.println("======== Hello World ========");

        return Response.ok().build();
    }
}
