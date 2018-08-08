package hluo.fun.playground.psi.server;

import hluo.fun.playground.psi.cluster.NodeManager;
import hluo.fun.playground.psi.compiler.ClassInfo;
import hluo.fun.playground.psi.execution.JobId;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.List;

@Path("/v1/job")
public class JobResource
{
    private final NodeManager nodeManager;
    private final JobManager jobManager;

    @Inject
    public JobResource(NodeManager nodeManager, JobManager jobManager)
    {
        this.nodeManager = nodeManager;
        this.jobManager = jobManager;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllJobs()
    {
        List<JobInfo> jobs = jobManager.getAllJobs();
        return Response.ok(jobs).build();
    }

    @GET
    @Path("{jobId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJobInfo(@PathParam("jobId") JobId jobId)
    {
        JobInfo jobInfo = jobManager.getJobInfo(jobId);
        return Response.ok(jobInfo).build();
    }

    @GET
    @Path("class/{jobId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJobClassInfo(@PathParam("jobId") JobId jobId)
    {
        ClassInfo classInfo = jobManager.getJobClassInfo(jobId);
        return Response.ok().entity(classInfo).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response submitFunction(String sourceCode, @Context HttpServletRequest servletRequest)
    {
        String className = servletRequest.getHeader("X-Pica-Class-Name");

        // compile source code
        JobInfo jobInfo = jobManager.addJob(className, sourceCode);

        return Response.ok(jobInfo).build();
    }

    @DELETE
    @Path("{jobId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTask(
            @PathParam("jobId") JobId jobId)
    {
        JobInfo jobInfo = jobManager.removeJob(jobId);
        return Response.ok().entity(jobInfo).build();
    }
}
