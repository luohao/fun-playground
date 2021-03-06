package hluo.fun.playground.psi.execution;

public enum TaskState
{
    /**
     * Task is started
     * FIXME: this state is needed when we are using async response...
     */
    STARTED(false),
    /**
     * Task is running.
     */
    RUNNING(false),
    /**
     * Task was canceled by a user.
     */
    CANCELED(true),
    /**
     * Task execution failed.
     */
    FAILED(true);

    TaskState(boolean doneState)
    {
        this.doneState = doneState;
    }

    private final boolean doneState;

    public boolean isDone()
    {
        return doneState;
    }
}
