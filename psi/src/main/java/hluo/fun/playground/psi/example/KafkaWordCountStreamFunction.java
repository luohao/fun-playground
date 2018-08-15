package hluo.fun.playground.psi.example;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hluo.fun.playground.psi.execution.StreamFunction;
import hluo.fun.playground.psi.execution.Tuple;
import hluo.fun.playground.psi.testing.kvs.KvsClient;
import hluo.fun.playground.psi.utils.KafkaSource;
import io.airlift.log.Logger;
import io.airlift.units.Duration;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * This is a stream function that does simple word counts.
 * <p>
 * 1. constructor generates a set of random words (~1k words, 10 chars per word)
 * 2. source() will poll data from kafka
 * 3. proc() will count the occurrences of the word
 * 4. sink() will sync the data into a remote key-value store
 */
public class KafkaWordCountStreamFunction
        implements StreamFunction
{
    private static final Logger log = Logger.get(WordCountStreamFunction.class);

    private final Map<String, Integer> countMap;
    private final KafkaSource kafkaSource;
    //private final KvsClient kvsClient;

    private long lastUpdateTimestamp;
    private static final Duration OUTPUT_INTERVAL = new Duration(5, TimeUnit.SECONDS);

    public KafkaWordCountStreamFunction()
    {
        countMap = new HashMap<>();
        // parse the configs
        Map<String, String> kafkaConfig = ImmutableMap.of();
        String topicName = "";
        kafkaSource = new KafkaSource(kafkaConfig, topicName);
        //kvsClient = new KvsClient();
    }

    @Override
    public List<Tuple> source()
    {
        List<ByteBuffer> batch = kafkaSource.fetchBatch();
        return batch.stream()
                .map(x -> WordTuple.fromByteBuffer(x))
                .collect(Collectors.toList());
    }

    @Override
    public List<Tuple> proc(Tuple tuple)
    {
        checkArgument(tuple instanceof WordTuple, "Tuple must be type of WordTuple");
        String key = ((WordTuple) tuple).getWord();
        if (countMap.get(key) == null) {
            countMap.put(key, 1);
        }
        else {
            Integer val = countMap.get(key);
            countMap.put(key, ++val);
        }

        return ImmutableList.of(tuple);
    }

    @Override
    public void sink(List<Tuple> list)
    {
        // printout the result every five seconds
        if (Duration.nanosSince(lastUpdateTimestamp).compareTo(OUTPUT_INTERVAL) > 0) {

            lastUpdateTimestamp = System.nanoTime();
        }
    }
}
