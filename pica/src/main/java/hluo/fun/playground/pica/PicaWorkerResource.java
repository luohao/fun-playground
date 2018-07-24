package hluo.fun.playground.pica;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class PicaWorkerResource
{
    // Assign function to the worker
    @POST
    @Path("start")
    @Produces(MediaType.APPLICATION_JSON)
    public Response startFunction()
    {
        System.out.println("======== Start function! ========");
        return Response.ok().build();
    }

    // List all running functions
    @GET
    @Path("list/functions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFunctionList()
    {
        System.out.println("======== List all functions! ========");
        return Response.ok().build();
    }

    @DELETE
    @Path("stop/{functionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response stopFunction(@PathParam("functionId") int functionId)
    {
        System.out.println("======== Stop function " + functionId + " ! ========");
        return Response.ok().build();
    }
}
