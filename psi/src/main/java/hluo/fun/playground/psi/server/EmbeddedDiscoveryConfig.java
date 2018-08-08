package hluo.fun.playground.psi.server;

import io.airlift.configuration.Config;

public class EmbeddedDiscoveryConfig
{
    private boolean enabled;

    public boolean isEnabled()
    {
        return enabled;
    }

    @Config("discovery-server.enabled")
    public EmbeddedDiscoveryConfig setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }
}
