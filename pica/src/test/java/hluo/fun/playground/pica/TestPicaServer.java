package hluo.fun.playground.pica;

import hluo.fun.playground.pica.compiler.ClassInfo;
import hluo.fun.playground.pica.compiler.CompilerUtils;
import org.testng.annotations.Test;

import static java.lang.Thread.sleep;
import static org.testng.AssertJUnit.assertNotNull;

public class TestPicaServer
{

    @Test
    public void testMaster()
            throws Exception
    {
        PicaServer master = new PicaServer(true);
        try {
            master.start();
            String className = "hluo.fun.playground.pica.example.TestFunctionAction";
            StringBuilder builder = new StringBuilder();
            String sourceCode = builder
                    .append("package hluo.fun.playground.pica.example;\n")
                    .append("import hluo.fun.playground.pica.FunctionAction;")
                    .append("public class TestFunctionAction\n")
                    .append("        implements FunctionAction\n")
                    .append("{\n")
                    .append("    private final String message;\n")
                    .append("    public TestFunctionAction(String message)\n")
                    .append("    {\n")
                    .append("        this.message = message;\n")
                    .append("    }\n")
                    .append("    @Override\n")
                    .append("    public void exec()\n")
                    .append("            throws Exception\n")
                    .append("    {\n")
                    .append("        System.out.println(\"hello from \" + message);\n")
                    .append("    }\n")
                    .append("}\n")
                    .toString();
            PicaClient client = new PicaClient(master.getBaseUrl());
            FunctionId id = client.submitFunction(className, sourceCode);
            assertNotNull("function id is null", id);

            client.listFunction().stream().forEach(x -> System.out.println(x));

            // get function info
            try {
                ClassInfo classInfo = ClientUtils.getClassInfo(master.getBaseUrl(), id);
                CompilerUtils compiler = new CompilerUtils();
                FunctionAction testFunction = (FunctionAction) compiler.loadClass(classInfo).getDeclaredConstructor(String.class).newInstance("test-pica-server-from-client");
                testFunction.exec();
            } catch (Exception e) {
                e.printStackTrace();
            }
            client.stopFunction(id);

            // wait for the function to stop
            sleep(3);
        }
        finally {
            master.close();
        }
    }

    @Test
    public void testWorker()
            throws Exception
    {
        PicaServer worker = new PicaServer(false);
        try {
            worker.start();
        }
        finally {
            worker.close();
        }
    }
}
