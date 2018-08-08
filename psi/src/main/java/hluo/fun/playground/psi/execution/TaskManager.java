package hluo.fun.playground.psi.execution;

import com.google.inject.Inject;
import hluo.fun.playground.psi.cluster.NodeManager;
import hluo.fun.playground.psi.compiler.ClassInfo;
import hluo.fun.playground.psi.compiler.CompilerUtils;
import hluo.fun.playground.psi.server.TaskUpdateRequest;
import io.airlift.http.client.HttpClient;
import io.airlift.http.client.JsonResponseHandler;
import io.airlift.http.client.Request;

import java.net.URI;

import static com.google.common.base.Preconditions.checkState;
import static io.airlift.http.client.HttpUriBuilder.uriBuilderFrom;
import static io.airlift.http.client.JsonResponseHandler.createJsonResponseHandler;
import static io.airlift.http.client.Request.Builder.prepareGet;
import static io.airlift.json.JsonCodec.jsonCodec;
import static java.util.Objects.requireNonNull;

/**
 * Task manager in worker node.
 * Make one abstract layer between TaskExecutor and JobManager, in case we manage multiple executor in one node.
 */
public class TaskManager
{
    private final TaskExecutor taskExecutor;
    private final HttpClient httpClient;
    private final URI self;

    // TODO: move streamFunction into taskInfo
    TaskInfo runningTask;
    StreamFunction streamFunction;
    CompilerUtils compilerUtils;

    @Inject
    public TaskManager(TaskExecutor taskExecutor, @ForScheduler HttpClient httpClient, NodeManager nodeManager)
    {
        this.taskExecutor = requireNonNull(taskExecutor, "taskExecutor is null");
        this.httpClient = requireNonNull(httpClient, "httpClient is null");
        this.self = requireNonNull(nodeManager, "nodeManager is null").getCurrentNode().getHttpUri();

        this.runningTask = null;
        this.streamFunction = null;
        compilerUtils = new CompilerUtils();
    }

    public synchronized TaskInfo updateTask(TaskId taskId, TaskUpdateRequest request)
    {
        // handle only create case
        // TODO: implement update case
        checkState(runningTask == null, "task already running");
        // TODO: verify nodeId
        // parse JobId
        JobId jobId = taskId.getJobId();
        // fetch class info from master
        TaskInfo taskInfo = TaskInfo.createInitialTask(taskId, self, request.getNodeId());
        try {
            this.streamFunction = fetchStreamFunction(jobId, request);
        }
        catch (Exception e) {
            // FIXME: handle exceptions correctly
            return new TaskInfo(taskId,
                    TaskStatus.failedWith(String.valueOf(taskId.getId()),
                            taskInfo.getTaskStatus().getVersion(),
                            self,
                            request.getNodeId()));
        }
        taskExecutor.assignTask(streamFunction);
        return taskInfo;
    }

    public synchronized TaskInfo cancelTask(TaskId taskId)
    {
        checkState(runningTask.getTaskId().equals(taskId), "taskId mismatch");
        checkState((runningTask != null) && (streamFunction != null), "no running task");
        TaskInfo currentTask = runningTask;
        taskExecutor.removeTask(streamFunction);
        runningTask = null;
        streamFunction = null;
        TaskInfo taskInfo = new TaskInfo(taskId,
                TaskStatus.canceledWith(String.valueOf(taskId.getId()),
                        currentTask.getTaskStatus().getVersion(),
                        self,
                        currentTask.getTaskStatus().getNodeId()));
        return taskInfo;
    }

    public TaskInfo getRunningTask()
    {
        return runningTask;
    }

    private StreamFunction fetchStreamFunction(JobId jobId, TaskUpdateRequest taskUpdateRequest)
            throws Exception
    {
        // fetch the stream function object from master
        Request request = prepareGet()
                .setUri(uriBuilderFrom(taskUpdateRequest.getMaster()).replacePath("/v1/job/" + jobId).build())
                .build();

        JsonResponseHandler<ClassInfo> responseHandler = createJsonResponseHandler(jsonCodec(ClassInfo.class));
        ClassInfo classInfo = httpClient.execute(request, responseHandler);

        return (StreamFunction) compilerUtils.loadClass(classInfo).newInstance();
    }
}
