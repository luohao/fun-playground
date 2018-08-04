package hluo.fun.playground.psi.execution;

import hluo.fun.playground.psi.server.TaskUpdateRequest;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static java.util.Objects.requireNonNull;

@Path("/v1/task")
public class TaskResource
{
    private final TaskManager taskManager;

    @Inject
    public TaskResource(TaskManager taskManager)
    {
        this.taskManager = requireNonNull(taskManager, "taskManager is null");
    }

    // create or update task
    @POST
    @Path("{taskId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createOrUpdateTask(@PathParam("taskId") TaskId taskId, TaskUpdateRequest taskUpdateRequest)
    {
        requireNonNull(taskUpdateRequest, "taskUpdateRequest is null");
        TaskInfo taskInfo = taskManager.updateTask(taskId, taskUpdateRequest);
        return Response.ok().entity(taskInfo).build();
    }

    // get the info of the task for the taskId
    @GET
    @Path("{taskId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getTaskInfo(@PathParam("taskId") final TaskId taskId)
    {
        TaskInfo taskInfo = taskManager.getRunningTask();
    }

    @DELETE
    @Path("{taskId}")
    @Produces(MediaType.APPLICATION_JSON)
    public TaskInfo deleteTask(
            @PathParam("taskId") TaskId taskId)
    {
        requireNonNull(taskId, "taskId is null");
        TaskInfo taskInfo = taskManager.cancelTask(taskId);
        return taskInfo;
    }
}
