package hluo.fun.playground.psi.server;

import io.airlift.configuration.Config;

import javax.validation.constraints.NotNull;

public class ServerConfig
{
    private boolean isGroupMaster = true;
    private String psiVersion = getClass().getPackage().getImplementationVersion();

    public boolean isGroupMaster()
    {
        return isGroupMaster;
    }

    @NotNull(message = "psi.version must be provided when it cannot be automatically determined")
    public String getPsiVersion()
    {
        return psiVersion;
    }

    @Config("master")
    public ServerConfig setGroupMaster(boolean isGroupMaster)
    {
        this.isGroupMaster = isGroupMaster;
        return this;
    }

    @Config("psi.version")
    public ServerConfig setPsiVersion(String psiVersion)
    {
        this.psiVersion = psiVersion;
        return this;
    }
}
