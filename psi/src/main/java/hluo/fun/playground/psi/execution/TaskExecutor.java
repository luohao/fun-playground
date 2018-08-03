package hluo.fun.playground.psi.execution;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public interface TaskExecutor
{
    @PostConstruct
    void start();

    @PreDestroy
    void stop();

    void assignTask(StreamFunction assignment);

    void removeTask(StreamFunction assignment);
}
