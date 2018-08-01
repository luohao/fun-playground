package hluo.fun.playground.psi.server;

import hluo.fun.playground.psi.test.TestNodeManager;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1/job/")
public class JobResource
{
    private final TestNodeManager nodeManager;

    @Inject
    public JobResource(TestNodeManager nodeManager, TestNodeManager nodeManager1)
    {
        this.nodeManager = nodeManager1;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFunctionList()
    {
        System.out.println("======== Hello World ========");

        // --- TEST CODE ---
        nodeManager.listServiceSelector();
        // --- TEST CODE ---

        return Response.ok().build();
    }
}
