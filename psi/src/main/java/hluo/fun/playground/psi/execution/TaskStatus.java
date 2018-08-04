package hluo.fun.playground.psi.execution;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.airlift.units.DataSize;
import io.airlift.units.Duration;

import java.net.URI;

import static com.google.common.base.MoreObjects.toStringHelper;
import static io.airlift.units.DataSize.Unit.BYTE;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class TaskStatus
{
    /**
     * The first valid version that will be returned for a remote task.
     */
    public static final long STARTING_VERSION = 1;

    private final String taskInstanceId;
    private final long version;
    private final String nodeId;

    @JsonCreator
    public TaskStatus(
            @JsonProperty("taskInstanceId") String taskInstanceId,
            @JsonProperty("version") long version,
            @JsonProperty("nodeId") String nodeId)
    {
        this.taskInstanceId = requireNonNull(taskInstanceId, "taskInstanceId is null");
        this.version = requireNonNull(version, "version is null");
        this.nodeId = requireNonNull(nodeId, "nodeId is null");
    }

    @JsonProperty
    public String getTaskInstanceId()
    {
        return taskInstanceId;
    }

    @JsonProperty
    public long getVersion()
    {
        return version;
    }

    @JsonProperty
    public String getNodeId()
    {
        return nodeId;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
                .add("taskInstanceId", taskInstanceId)
                .toString();
    }

    public static TaskStatus createInitialTaskStatus(String taskInstanceId, String nodeId)
    {
        return new TaskStatus(
                taskInstanceId,
                STARTING_VERSION,
                nodeId
               );
    }
}
