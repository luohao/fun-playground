package hluo.fun.playground.psi.server;

import io.airlift.configuration.Config;

public class ServerConfig
{
    private boolean isGroupMaster = true;

    public boolean isGroupMaster()
    {
        return isGroupMaster;
    }

    @Config("isGroupMaster")
    public ServerConfig setGroupMaster(boolean isGroupMaster)
    {
        this.isGroupMaster = isGroupMaster;
        return this;
    }
}
