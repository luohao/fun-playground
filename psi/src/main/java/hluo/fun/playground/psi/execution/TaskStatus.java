package hluo.fun.playground.psi.execution;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

public class TaskStatus
{
    /**
     * The first valid version that will be returned for a remote task.
     */
    public static final long STARTING_VERSION = 1;

    private final String taskInstanceId;
    private final long version;
    private final URI self;
    private final String nodeId;

    private final TaskState state;

    @JsonCreator
    public TaskStatus(
            @JsonProperty("taskInstanceId") String taskInstanceId,
            @JsonProperty("version") long version,
            @JsonProperty("self") URI self,
            @JsonProperty("nodeId") String nodeId,
            @JsonProperty("state") TaskState state
    )
    {
        this.taskInstanceId = requireNonNull(taskInstanceId, "taskInstanceId is null");
        this.version = requireNonNull(version, "version is null");
        this.self = requireNonNull(self, "self is null");
        this.nodeId = requireNonNull(nodeId, "nodeId is null");

        this.state = requireNonNull(state, "state is null");
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
    public URI getSelf()
    {
        return self;
    }

    @JsonProperty
    public String getNodeId()
    {
        return nodeId;
    }

    @JsonProperty
    public TaskState getState()
    {
        return state;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
                .add("taskInstanceId", taskInstanceId)
                .add("version", version)
                .add("self", self)
                .add("nodeId", nodeId)
                .add("state", state)
                .toString();
    }

    public static TaskStatus createInitialTaskStatus(String taskInstanceId, URI locatoin, String nodeId)
    {
        // FIXME: when switched to async start of the task, the initial state should be STARTED
        return new TaskStatus(
                taskInstanceId,
                STARTING_VERSION,
                locatoin,
                nodeId,
                TaskState.RUNNING
        );
    }

    public static TaskStatus canceledWith(String taskInstanceId, long version, URI locatoin, String nodeId)
    {
        return new TaskStatus(
                taskInstanceId,
                version,
                locatoin,
                nodeId,
                TaskState.CANCELED
        );
    }

    public static TaskStatus failedWith(String taskInstanceId, long version, URI locatoin, String nodeId)
    {
        return new TaskStatus(
                taskInstanceId,
                version,
                locatoin,
                nodeId,
                TaskState.FAILED
        );
    }
}
