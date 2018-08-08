package hluo.fun.playground.psi.cluster;

import com.google.common.net.HostAndPort;

import java.net.URI;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;

/**
 * A node is a server in a cluster than can process queries.
 */
public class PsiNode
        implements Node
{
    private final String nodeIdentifier;
    private final URI httpUri;
    private final NodeVersion nodeVersion;
    private final boolean master;

    public PsiNode(String nodeIdentifier, URI httpUri, NodeVersion nodeVersion, boolean master)
    {
        nodeIdentifier = emptyToNull(nullToEmpty(nodeIdentifier).trim());
        this.nodeIdentifier = nodeIdentifier;
        this.httpUri = httpUri;
        this.nodeVersion = nodeVersion;
        this.master = master;
    }

    @Override
    public String getNodeIdentifier()
    {
        return nodeIdentifier;
    }

    @Override
    public String getVersion()
    {
        return nodeVersion.getVersion();
    }

    public NodeVersion getNodeVersion()
    {
        return nodeVersion;
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
    public boolean isMaster()
    {
        return master;
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
        PsiNode o = (PsiNode) obj;
        return nodeIdentifier.equals(o.nodeIdentifier);
    }

    @Override
    public int hashCode()
    {
        return nodeIdentifier.hashCode();
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
                .add("nodeIdentifier", nodeIdentifier)
                .add("httpUri", httpUri)
                .add("nodeVersion", nodeVersion)
                .toString();
    }
}
