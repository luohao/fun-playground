package hluo.fun.playground.psi.cluster;

import java.io.Closeable;

public interface PsiCluster
        extends Closeable
{
    @Override
    void close();

    int getNodeCount();
}
