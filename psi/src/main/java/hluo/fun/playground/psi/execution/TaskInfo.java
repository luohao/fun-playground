package hluo.fun.playground.psi.execution;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;

import java.net.URI;
import java.util.List;

import static com.google.common.base.MoreObjects.toStringHelper;

@Immutable
public class TaskInfo
{
    private final TaskId taskId;
    private final TaskStatus taskStatus;
    // TODO: more stats here

    public TaskInfo(
            @JsonProperty("taskId") TaskId taskId,
            @JsonProperty("taskStatus") TaskStatus taskStatus)
    {
        this.taskId = taskId;
        this.taskStatus = taskStatus;
    }

    @JsonProperty
    public TaskId getTaskId()
    {
        return taskId;
    }

    @JsonProperty
    public TaskStatus getTaskStatus()
    {
        return taskStatus;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
                .add("taskId", getTaskId())
                .add("version", taskStatus.getVersion())
                .toString();
    }

    public static TaskInfo createInitialTask(TaskId taskId, String nodeId)
    {
        return new TaskInfo(
                taskId,
                TaskStatus.createInitialTaskStatus(String.valueOf(taskId.getId()), nodeId)
                );
    }
}
