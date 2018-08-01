package hluo.fun.playground.psi.cluster;

import com.google.common.net.HostAndPort;

import java.net.URI;

public interface Node
{
    HostAndPort getHostAndPort();

    URI getHttpUri();

    String getNodeIdentifier();
}
