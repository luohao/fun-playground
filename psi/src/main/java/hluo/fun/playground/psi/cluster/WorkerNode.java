package hluo.fun.playground.psi.cluster;

import com.google.common.net.HostAndPort;

import java.net.URI;

public class WorkerNode
    implements Node
{
    private final String nodeIdentifier;
    private final URI httpUri;
    private final boolean coordinator;
    private final boolean master;

    public WorkerNode(String nodeIdentifier, URI httpUri, boolean coordinator, boolean master) {
        this.nodeIdentifier = nodeIdentifier;
        this.httpUri = httpUri;
        this.coordinator = coordinator;
        this.master = master;
    }

    @Override
    public String getNodeIdentifier()
    {
        return nodeIdentifier;
    }

    @Override
    public URI getHttpUri()
    {
        return httpUri;
    }

    @Override
    public HostAndPort getHostAndPort()
    {
        return HostAndPort.fromString(httpUri.toString());
    }

    @Override
    public boolean isCoordinator()
    {
        return coordinator;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        WorkerNode o = (WorkerNode) obj;
        return nodeIdentifier.equals(o.nodeIdentifier);
    }
}
