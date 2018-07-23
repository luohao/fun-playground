package hluo.fun.playground.pica;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;

import static com.google.common.base.Preconditions.checkState;
import static java.util.concurrent.Executors.newCachedThreadPool;

public class TaskExecutor
{
    private final ExecutorService executor;
    private final int runnerThreads;
    private final BlockingQueue<FunctionAction> taskQueue;
    private volatile boolean closed;

    public TaskExecutor(int runnerThreads)
    {
        this.executor = newCachedThreadPool();
        this.taskQueue = new LinkedBlockingQueue();
        this.runnerThreads = runnerThreads;
    }

    public synchronized void start()
    {
        checkState(!closed, "TaskExecutor is closed");
        for (int i = 0; i < runnerThreads; i++) {
            addRunnerThread();
        }
    }

    public synchronized void stop()
    {
        closed = true;
        executor.shutdownNow();
    }

    public synchronized void addTask(FunctionAction task)
    {
        taskQueue.offer(task);
    }

    private synchronized void addRunnerThread()
    {
        try {
            executor.execute(new TaskRunner());
        }
        catch (RejectedExecutionException ignored) {
        }
    }

    private class TaskRunner
            implements Runnable
    {

        @Override
        public void run()
        {
            while (!closed && !Thread.currentThread().isInterrupted()) {
                // dequeue next take from queue
                try {
                    FunctionAction func = taskQueue.take();
                    func.exec();
                } catch (Exception e) {
                    if (!(e instanceof InterruptedException)) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

