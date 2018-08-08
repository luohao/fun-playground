package hluo.fun.playground.psi.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import hluo.fun.playground.psi.execution.JobId;
import hluo.fun.playground.psi.execution.TaskInfo;

import java.util.List;

import static com.google.common.base.MoreObjects.toStringHelper;

public class JobInfo
{
    private JobId jobId;
    private List<TaskInfo> taskInfos;

    @JsonCreator
    public JobInfo(
            @JsonProperty("jobId") JobId jobId,
            @JsonProperty("taskInfos") List<TaskInfo> taskInfos)
    {
        this.jobId = jobId;
        this.taskInfos = taskInfos;
    }

    @JsonProperty
    public JobId getJobId()
    {
        return jobId;
    }

    @JsonProperty
    public List<TaskInfo> getTaskInfos()
    {
        return taskInfos;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
                .add("jobId", getJobId())
                .toString();
    }
}
