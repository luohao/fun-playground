package hluo.fun.playground.pica;

import org.testng.annotations.Test;

import static java.lang.Thread.sleep;

public class TestTaskExecutor
{
    private final int PARALLELISM = 4;

    @Test
    void test()
    {
        TaskExecutor executor = new TaskExecutor(PARALLELISM);

        for (int i = 0; i < PARALLELISM * 4; ++i) {
            executor.addTask(new TestFunctionAction("function-action-" + i));
        }
        System.out.println("start testing");
        executor.start();
        try {
            sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("failed to wait for executor");
        }
        executor.stop();
        System.out.println("done testing");
    }

    private class TestFunctionAction
            implements FunctionAction
    {
        private final String message;

        public TestFunctionAction(String message)
        {
            this.message = message;
        }

        @Override
        public void exec()
                throws Exception
        {
            System.out.println("hello from " + message);
            sleep(1000);
        }
    }
}
