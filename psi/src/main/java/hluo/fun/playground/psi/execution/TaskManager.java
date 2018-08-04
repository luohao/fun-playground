package hluo.fun.playground.psi.execution;

import com.google.inject.Inject;
import hluo.fun.playground.psi.server.TaskUpdateRequest;
import io.airlift.http.client.HttpClient;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

/**
 * Task manager in worker node.
 * Make one abstract layer between TaskExecutor and JobManager, in case we manage multiple executor in one node.
 */
public class TaskManager
{
    private final TaskExecutor taskExecutor;
    // TODO: move streamFunction into taskInfo
    TaskInfo runningTask;
    StreamFunction streamFunction;
    private final HttpClient httpClient;

    @Inject
    public TaskManager(TaskExecutor taskExecutor, @ForTaskClassInfo HttpClient httpClient)
    {
        this.taskExecutor = requireNonNull(taskExecutor, "taskExecutor is null");
        this.httpClient = requireNonNull(httpClient, "httpClient is null");
        this.runningTask = null;
        this.streamFunction = null;
    }

    public synchronized TaskInfo updateTask(TaskId taskId, TaskUpdateRequest request)
    {
        // handle only create case
        // TODO: implement update case
        checkState(runningTask == null, "task already running");
        // parse JobId
        JobId jobId = taskId.getJobId();
        // fetch class info from master

        StreamFunction streamFunction = fetchStreamFunction(jobId, request);

        taskExecutor.assignTask(streamFunction);
        TaskInfo taskInfo = TaskInfo.createInitialTask(taskId, request.getNodeId());
        return null;
    }

    public synchronized TaskInfo cancelTask(TaskId taskId)
    {
        checkState(runningTask.getTaskId().equals(taskId), "taskId mismatch");
        checkState((runningTask != null) && (streamFunction != null), "no running task");
        TaskInfo taskInfo = runningTask;
        taskExecutor.removeTask(streamFunction);
        runningTask = null;
        streamFunction = null;
        return taskInfo;
    }

    public TaskInfo getRunningTask()
    {
        return runningTask;
    }

    private StreamFunction fetchStreamFunction(JobId jobId, TaskUpdateRequest request) {
        // fetch the stream function object from master
        return null;
    }
}
