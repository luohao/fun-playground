package hluo.fun.playground.pica;

import pl.joegreen.lambdaFromString.LambdaFactory;
import pl.joegreen.lambdaFromString.TypeReference;

public final class CompilerUtils
{
    private static final LambdaFactory lambdaFactory = LambdaFactory.get();
    public static PicaFunction compileLambda(String lambda)
            throws Exception
    {
        return lambdaFactory.createLambda(lambda, new TypeReference<PicaFunction>() {});
    }
}
