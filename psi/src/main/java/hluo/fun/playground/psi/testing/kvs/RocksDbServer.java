package hluo.fun.playground.psi.testing.kvs;

import hluo.fun.playground.psi.testing.TestingPsiCluster;
import io.airlift.log.Logger;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public class RocksDbServer
        implements KeyValueStore
{
    private static final Logger log = Logger.get(TestingPsiCluster.class);
    private final String rocksdbDir;
    private RocksDB db = null;

    public RocksDbServer(String rocksdbDir) {
        this.rocksdbDir = rocksdbDir;
    }

    @Override
    public void start()
            throws Exception
    {
        RocksDB.loadLibrary();
        final Options options = new Options().setCreateIfMissing(true);
        this.db = RocksDB.open(options, rocksdbDir);
    }

    @Override
    public void close()
    {
        if (db != null) {
            db.close();
        }
    }

    @Override
    public KvsResponse put(KvsRequest request)
    {
        checkArgument(request.getOperation() == KvsOperation.PUT);
        checkArgument(request.getValue().isPresent(), "value is missing");
        checkState(this.db != null, "db is null");

        try {
            db.put(request.getKey(), request.getValue().get());
            return KvsResponse.succeed();
        }
        catch (RocksDBException e) {
            log.warn(e.getMessage());
            return KvsResponse.failWith(e.getMessage());
        }
    }

    @Override
    public KvsResponse get(KvsRequest request)
    {
        checkArgument(request.getOperation() == KvsOperation.GET);
        checkState(this.db != null, "db is null");

        try {
            byte[] ret = db.get(request.getKey());
            return KvsResponse.succeedWithValue(ret);
        }
        catch (RocksDBException e) {
            log.warn(e.getMessage());
            return KvsResponse.failWith(e.getMessage());
        }
    }

    @Override
    public KvsResponse delete(KvsRequest request)
    {
        checkArgument(request.getOperation() == KvsOperation.DELETE);
        checkState(this.db != null, "db is null");

        try {
            db.delete(request.getKey());
            return KvsResponse.succeed();
        }
        catch (RocksDBException e) {
            log.warn(e.getMessage());
            return KvsResponse.failWith(e.getMessage());
        }
    }
}
