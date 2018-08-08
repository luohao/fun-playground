package hluo.fun.playground.psi.example;

import com.google.common.collect.ImmutableList;
import hluo.fun.playground.psi.execution.StreamFunction;
import hluo.fun.playground.psi.execution.Tuple;
import io.airlift.log.Logger;
import io.airlift.units.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * This is a stream function that does simple word counts.
 * <p>
 * 1. constructor generates a set of random words (~1k words, 10 chars per word)
 * 2. sourc() will pick a word at random
 * 3. proc() will count the occurrences of the word
 * 4. sink() will printout the info every 5 seconds
 */
public class WordCountStreamFunction
        implements StreamFunction
{
    private static final Logger log = Logger.get(WordCountStreamFunction.class);

    private static final int ARRAY_LENGTH = 1024;
    private static final int WORD_LENGTH = 10;
    private final String[] words = new String[ARRAY_LENGTH];
    private final Random rnd = new Random(31);

    private final Map<String, Integer> countMap;

    private long lastUpdateTimestamp;
    private static final Duration OUTPUT_INTERVAL = new Duration(5, TimeUnit.SECONDS);

    public WordCountStreamFunction()
    {
        // generate the word map
        RandomString randomString = new RandomString(WORD_LENGTH);
        for (int i = 0; i < ARRAY_LENGTH; i++) {
            words[i] = randomString.nextString();
        }
        countMap = new HashMap<>();
        lastUpdateTimestamp = System.nanoTime();
    }

    @Override
    public List<Tuple> source()
    {
        return ImmutableList.of(new WordTuple(words[rnd.nextInt(ARRAY_LENGTH)]));
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
            log.info("\n========= WORD COUNT =========");
            log.info(countMap.toString());
            log.info("==============================\n");
            lastUpdateTimestamp = System.nanoTime();
        }
    }

    public class WordTuple
            implements Tuple
    {
        private final String word;

        WordTuple(String word)
        {
            this.word = word;
        }

        String getWord()
        {
            return word;
        }
    }

    // Utils class to generate random String at given length
    public static class RandomString
    {
        private final char[] symbols;

        private final Random random = new Random();

        private final char[] buf;

        public RandomString(int length)
        {
            // Construct the symbol set
            StringBuilder tmp = new StringBuilder();
            for (char ch = '0'; ch <= '9'; ++ch) {
                tmp.append(ch);
            }

            for (char ch = 'a'; ch <= 'z'; ++ch) {
                tmp.append(ch);
            }

            symbols = tmp.toString().toCharArray();
            if (length < 1) {
                throw new IllegalArgumentException("length < 1: " + length);
            }

            buf = new char[length];
        }

        public String nextString()
        {
            for (int idx = 0; idx < buf.length; ++idx) {
                buf[idx] = symbols[random.nextInt(symbols.length)];
            }

            return new String(buf);
        }
    }
}
