package hluo.fun.playground.psi.server;

import io.airlift.configuration.Config;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class ServerConfig
{
    private boolean isGroupMaster = true;
    private String psiVersion = getClass().getPackage().getImplementationVersion();
    private int maxWorkerThreads = 3;

    public boolean isGroupMaster()
    {
        return isGroupMaster;
    }

    @NotNull(message = "psi.version must be provided when it cannot be automatically determined")
    public String getPsiVersion()
    {
        return psiVersion;
    }

    @Min(3)
    public int getMaxWorkerThreads() { return maxWorkerThreads; }


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

    @Config("task.max-worker-threads")
    @Min(3)
    public ServerConfig setMaxWorkerThreads(int maxWorkerThreads)
    {
        this.maxWorkerThreads = maxWorkerThreads;
        return this;
    }
}
