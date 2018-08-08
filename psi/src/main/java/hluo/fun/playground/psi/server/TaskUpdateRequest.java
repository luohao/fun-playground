package hluo.fun.playground.psi.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

public class TaskUpdateRequest
{
    // TODO: implement update request for updating the task (e.g., update the version).

    private final URI master;
    private final String nodeId;

    @JsonCreator
    public TaskUpdateRequest(@JsonProperty("master") URI master, @JsonProperty("nodeId") String nodeId)
    {
        this.master = requireNonNull(master, "master is null");
        this.nodeId = requireNonNull(nodeId, "nodeId is null");
    }

    @JsonProperty
    public URI getMaster()
    {
        return master;
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
                .add("master", master)
                .add("nodeId", nodeId)
                .toString();
    }
}
