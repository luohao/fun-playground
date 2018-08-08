package hluo.fun.playground.psi.cluster;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.List;

public class ClusterInfo
{
    private final List<URI> masters;
    private final List<URI> workers;

    @JsonCreator
    public ClusterInfo(@JsonProperty("master") List<URI> masters,
            @JsonProperty("workers") List<URI> workers)
    {
        this.masters = masters;
        this.workers = workers;
    }

    @JsonProperty
    public List<URI> getMasters()
    {
        return masters;
    }

    @JsonProperty
    public List<URI> getWorkers()
    {
        return workers;
    }
}
