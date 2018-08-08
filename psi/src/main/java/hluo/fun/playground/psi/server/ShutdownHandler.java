package hluo.fun.playground.psi.server;

import io.airlift.bootstrap.LifeCycleManager;
import io.airlift.log.Logger;

import javax.inject.Inject;

import static java.util.Objects.requireNonNull;

public class ShutdownHandler
{
    private static final Logger log = Logger.get(ShutdownHandler.class);

    private final LifeCycleManager lifeCycleManager;
    private final boolean master;

    @Inject
    public ShutdownHandler(LifeCycleManager lifeCycleManager, ServerConfig serverConfig)
    {
        this.lifeCycleManager = requireNonNull(lifeCycleManager, "lifeCycleManager is null");
        this.master = requireNonNull(serverConfig, "serverConfig is null").isGroupMaster();
    }

    public synchronized void requestShutdown()
    {
        // just shut it down...
        // TODO: fix shutdown handler...
        try {
            log.info("Shutting down server...");
            // FIXME: can't seem to stop the server because of http server executor
            lifeCycleManager.stop();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
