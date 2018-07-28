package hluo.fun.playground.pica;

import hluo.fun.playground.pica.compiler.ClassInfo;
import hluo.fun.playground.pica.compiler.CompilerUtils;
import org.testng.annotations.Test;

public class TestCompilerUtils
{
    @Test
    void test()
    {
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

        CompilerUtils compiler = new CompilerUtils();
        try {
            ClassInfo classInfo = compiler.compileSingleSource(className, sourceCode);
            FunctionAction testFunction = (FunctionAction) compiler.loadClass(classInfo).getDeclaredConstructor(String.class).newInstance("TestCompilerUtils");
            testFunction.exec();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
