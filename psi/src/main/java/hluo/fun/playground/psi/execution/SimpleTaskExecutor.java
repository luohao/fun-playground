package hluo.fun.playground.psi.execution;

import io.airlift.concurrent.SetThreadName;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static io.airlift.concurrent.Threads.threadsNamed;
import static java.util.concurrent.Executors.newCachedThreadPool;

public class SimpleTaskExecutor
        implements TaskExecutor
{
    private static final int MAX_QUEUE_SIZE = 64 * 1024;

    // runnerThreads = 1 * source + 1 * sink + 1 * processor
    private final int runnerThreads;
    private final ExecutorService executor;

    // use Function action to resolve some race condition when adding/removing task
    private final BlockingQueue<FunctionAction> incoming;
    private final BlockingQueue<FunctionAction> outgoing;
    private final AtomicReference<StreamFunction> task = new AtomicReference<>(null);   // initialized with null
    private final AtomicBoolean closed = new AtomicBoolean(false);// initialized with false

    public SimpleTaskExecutor(int runnerThreads)
    {
        checkArgument(runnerThreads > 2, "runnerThreads must be at least 3");

        this.runnerThreads = runnerThreads;
        this.executor = newCachedThreadPool(threadsNamed("task-executor-%s"));
        // use bounded queue for back pressure
        this.incoming = new ArrayBlockingQueue(MAX_QUEUE_SIZE);
        this.outgoing = new ArrayBlockingQueue(MAX_QUEUE_SIZE);
    }

    @PostConstruct
    public synchronized void start()
    {
        checkState(!closed.get(), "TaskExecutor is closed");
        // source worker
        addWorkerThread(new SourceWorker());

        // sink worker
        addWorkerThread(new SinkWorker());

        // processor
        for (int i = 2; i < runnerThreads; i++) {
            addWorkerThread(new TaskWorker());
        }
    }

    @PreDestroy
    public synchronized void stop()
    {
        closed.set(true);
        // TODO: Graceful shutdown: wait until both queues are empty
        executor.shutdownNow();
    }

    public void assignTask(StreamFunction assignment)
    {
        checkState(task.compareAndSet(null, assignment), "task already running");
    }

    public void removeTask(StreamFunction assignment)
    {

        checkState(task.compareAndSet(assignment, null), "no active task");
    }

    private synchronized void addWorkerThread(Runnable worker)
    {
        try {
            executor.execute(worker);
        }
        catch (RejectedExecutionException ignored) {
        }
    }

    // Worker threads
    private class TaskWorker
            implements Runnable
    {
        @Override
        public void run()
        {
            try (SetThreadName ignored = new SetThreadName("TaskWorker")) {
                while (!closed.get() &&
                        !Thread.currentThread().isInterrupted()) {
                    try {
                        FunctionAction action = incoming.take();
                        List<FunctionAction> out = action.proc();
                        out.stream().forEach(x -> {
                            try {
                                outgoing.put(x);
                            }
                            catch (InterruptedException e) {
                                // throw exception so outer try/catch block will catch it
                                throw new RuntimeException(e);
                            }
                        });
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
            finally {
                // unless we have been closed, we need to restart this thread
                if (!closed.get()) {
                    addWorkerThread(new SourceWorker());
                }
            }
        }
    }

    private class SourceWorker
            implements Runnable
    {
        @Override
        public void run()
        {
            try (SetThreadName ignored = new SetThreadName("SourceWorker")) {
                while (!closed.get() &&
                        !Thread.currentThread().isInterrupted()) {
                    StreamFunction function = task.get();
                    // if no function assigned
                    if (function == null) {
                        continue;
                    }

                    // fetch incoming Tuples
                    // TODO: exception handling?
                    List<Tuple> incomingTuples = function.source();

                    // enqueue incoming tuples
                    incomingTuples.stream()
                            .map(x -> new FunctionAction(x, function))
                            .forEach(x -> {
                                try {
                                    incoming.put(x);
                                }
                                catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    return;
                                }
                            });
                }
            }
            finally {
                // unless we have been closed, we need to restart this thread
                if (!closed.get()) {
                    addWorkerThread(new SourceWorker());
                }
            }
        }
    }

    private class SinkWorker
            implements Runnable
    {
        @Override
        public void run()
        {
            try (SetThreadName ignored = new SetThreadName("SinkWorker")) {
                while (!closed.get() &&
                        !Thread.currentThread().isInterrupted()) {
                    try {
                        FunctionAction action = outgoing.take();
                        action.sink();
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
            finally {
                // unless we have been closed, we need to restart this thread
                if (!closed.get()) {
                    addWorkerThread(new SourceWorker());
                }
            }
        }
    }
}
