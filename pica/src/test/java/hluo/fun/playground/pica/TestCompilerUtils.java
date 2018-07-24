package hluo.fun.playground.pica;

import org.testng.annotations.Test;

import static com.google.common.base.Throwables.throwIfUnchecked;

public class TestCompilerUtils
{
    @Test
    void testBasicLambda() {
        try {
            PicaFunction function = CompilerUtils.compileLambda("x -> { String s = (String)x; System.out.println(s.length()); }");
            function.process("abcd");
        } catch (Exception e) {
            throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }
}
