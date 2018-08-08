package hluo.fun.playground.psi.execution;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import hluo.fun.playground.psi.Utils.IdHelper;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Integer.parseInt;

/**
 * TaskId consists of two parts: job id and instance id.
 */
public class TaskId
{
    private final String fullId;

    @JsonCreator
    public static TaskId valueOf(String taskId)
    {
        return new TaskId(taskId);
    }

    public TaskId(String fullId)
    {
        this.fullId = fullId;
    }

    public TaskId(JobId jobId, int id)
    {
        checkArgument(id >= 0, "instance id is negative");
        this.fullId = jobId + "." + id;
    }

    public JobId getJobId()
    {
        return new JobId(IdHelper.parseDottedId(fullId, 2, "taskId").get(0));
    }

    public int getId() { return parseInt(IdHelper.parseDottedId(fullId, 2, "taskId").get(1)); }

    @Override
    @JsonValue
    public String toString()
    {
        return fullId;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(fullId);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TaskId other = (TaskId) obj;
        return Objects.equals(this.fullId, other.fullId);
    }
}
