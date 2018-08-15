package hluo.fun.playground.psi.testing.kvs;

public interface KeyValueStore
{
    void start() throws Exception;

    void close();

    KvsResponse put(KvsRequest request);

    KvsResponse get(KvsRequest request);

    KvsResponse delete(KvsRequest request);
}
