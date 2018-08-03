package hluo.fun.playground.psi.execution;

import hluo.fun.playground.psi.example.WordCountStreamFunction;
import org.testng.annotations.Test;

import static java.lang.Thread.sleep;

public class TestTaskExecutor
{
    @Test
    void testSimpleTaskExecutor() throws Exception {
        TaskExecutor executor = new SimpleTaskExecutor(3);

        executor.start();
        StreamFunction streamFunction = new WordCountStreamFunction();
        executor.assignTask(streamFunction);

        sleep(10000);

        executor.removeTask(streamFunction);
        executor.stop();
    }

}
