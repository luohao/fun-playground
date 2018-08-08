package hluo.fun.playground.psi.server;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.google.inject.Inject;
import hluo.fun.playground.psi.cluster.Node;
import hluo.fun.playground.psi.cluster.NodeManager;
import hluo.fun.playground.psi.compiler.ClassInfo;
import hluo.fun.playground.psi.compiler.CompilerUtils;
import hluo.fun.playground.psi.execution.ForScheduler;
import hluo.fun.playground.psi.execution.JobId;
import hluo.fun.playground.psi.execution.TaskId;
import hluo.fun.playground.psi.execution.TaskInfo;
import hluo.fun.playground.psi.execution.TaskState;
import io.airlift.http.client.FullJsonResponseHandler;
import io.airlift.http.client.HttpClient;
import io.airlift.http.client.JsonResponseHandler;
import io.airlift.http.client.Request;
import io.airlift.json.JsonCodec;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.airlift.http.client.FullJsonResponseHandler.createFullJsonResponseHandler;
import static io.airlift.http.client.HttpUriBuilder.uriBuilderFrom;
import static io.airlift.http.client.JsonBodyGenerator.jsonBodyGenerator;
import static io.airlift.http.client.JsonResponseHandler.createJsonResponseHandler;
import static io.airlift.http.client.Request.Builder.prepareDelete;
import static io.airlift.http.client.Request.Builder.preparePost;
import static java.util.Objects.requireNonNull;

public class JobManager
{
    private final NodeManager nodeManager;
    private final JsonCodec<TaskUpdateRequest> updateRequestCodec;
    private final JsonCodec<TaskInfo> taskInfoCodec;
    private final HttpClient httpClient;

    private final Map<JobId, JobInfo> jobInfoMap;
    private final Map<JobId, ClassInfo> classInfoMap;
    private final CompilerUtils compilerUtils;
    // TODO: implement job id generator
    private int lastJobId;

    @Inject
    public JobManager(NodeManager nodeManager,
            JsonCodec<TaskUpdateRequest> updateRequestCodec,
            JsonCodec<TaskInfo> taskInfoCodec,
            @ForScheduler HttpClient httpClient)
    {
        this.nodeManager = requireNonNull(nodeManager, "nodeManager is null");
        this.updateRequestCodec = requireNonNull(updateRequestCodec, "updateRequestCodec is null");
        this.taskInfoCodec = requireNonNull(taskInfoCodec, "taskInfoCodec is null");
        this.httpClient = requireNonNull(httpClient, "taskInfoCodec is null");

        this.jobInfoMap = new ConcurrentHashMap<>();
        this.classInfoMap = new ConcurrentHashMap<>();
        this.compilerUtils = new CompilerUtils();
        this.lastJobId = 0;
    }

    public JobInfo addJob(String className, String sourceCode)
    {
        JobId jobId = nextJobId();
        ClassInfo classInfo = compilerUtils.compileSingleSource(className, sourceCode);
        classInfoMap.put(jobId, classInfo);

        // assign task to worker nodes
        // TODO: a more complex, full blown job manager...
        Set<Node> activeNodes = nodeManager.getNodes(NodeState.ACTIVE);
        AtomicInteger taskInstanceId = new AtomicInteger(0);
        // assign the task to worker node
        List<TaskInfo> taskInfos = activeNodes.stream()
                .filter(x -> !nodeManager.getMasters().contains(x))
                .map(x -> {
                    Node masterNode = nodeManager.getCurrentNode();
                    int id = taskInstanceId.getAndIncrement();
                    TaskId taskId = new TaskId(jobId, id);
                    TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest(masterNode.getHttpUri(), masterNode.getNodeIdentifier());

                    Request request = preparePost()
                            .setUri(uriBuilderFrom(x.getHttpUri()).replacePath("/v1/task/" + taskId).build())
                            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
                            .setBodyGenerator(jsonBodyGenerator(updateRequestCodec, taskUpdateRequest))
                            .build();
                    JsonResponseHandler<TaskInfo> responseHandler = createJsonResponseHandler(taskInfoCodec);
                    // FIXME: use async response and check for task status
                    // FIXME: a better error handling...

                    TaskInfo taskInfo = httpClient.execute(request, responseHandler);
                    checkState(taskInfo.getTaskStatus().getState() == TaskState.RUNNING, "Task " + taskInfo.getTaskId() + " is not running");
                    return taskInfo;
                }).collect(toImmutableList());
        JobInfo jobInfo = new JobInfo(jobId, taskInfos);
        jobInfoMap.put(jobId, jobInfo);
        return jobInfo;
    }

    public JobInfo removeJob(JobId jobId)
    {
        JobInfo jobInfo = jobInfoMap.get(jobId);

        // remove all tasks from workers
        List<TaskInfo> taskInfos = jobInfo.getTaskInfos().stream()
                .map(x -> {
                    // cancel the task in the worker
                    Request request = prepareDelete()
                            .setUri(uriBuilderFrom(x.getTaskStatus().getSelf()).replacePath("/v1/task/" + x.getTaskId()).build())
                            .build();
                    JsonResponseHandler<TaskInfo> responseHandler = createJsonResponseHandler(taskInfoCodec);
                    TaskInfo taskInfo = httpClient.execute(request, responseHandler);

                    // check the status of the task
                    // FIXME: a better error handling...
                    checkState(taskInfo.getTaskStatus().getState().isDone(), "Failed to cancel task " + taskInfo.getTaskId());

                    return taskInfo;
                })
                .collect(toImmutableList());
        return new JobInfo(jobId, taskInfos);
    }

    public List<JobInfo> getAllJobs()
    {
        return jobInfoMap.values().stream().collect(toImmutableList());
    }

    private JobId nextJobId()
    {
        int id = lastJobId;
        ++lastJobId;
        return JobId.valueOf(String.valueOf(id));
    }

    public ClassInfo getJobClassInfo(JobId jobId)
    {
        return classInfoMap.get(jobId);
    }

    public JobInfo getJobInfo(JobId jobId)
    {
        return jobInfoMap.get(jobId);
    }
}
