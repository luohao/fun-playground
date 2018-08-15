package hluo.fun.playground.psi.testing;

import hluo.fun.playground.psi.utils.RandomString;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;

public final class TestUtils
{
    private TestUtils() {}

    // Find unused port
    public static int findUnusedPort()
            throws IOException
    {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    // Convert Map to Properties
    public static Properties toProperties(Map<String, String> config)
    {
        Properties properties = new Properties();
        config.forEach((k, v) -> properties.setProperty(k, v));
        return properties;
    }

    // Load testing topics into TestingKafkaServer. The testing topics consists of random words.
    public static Map<String, Integer> loadTestingTopic(
            TestingKafkaServer testingKafkaServer,
            String topicName,
            int totalWords,
            int wordLength,
            int count)
            throws Exception
    {
        Map<String, Integer> countMap = new HashMap();
        RandomString randomString = new RandomString(wordLength);
        // prepare a set of random strings
        List<String> words = IntStream.range(0, totalWords)
                .mapToObj(x -> randomString.nextString())
                .collect(toImmutableList());

        testingKafkaServer.createTopics(topicName);

        try (TestingKafkaServer.CloseableProducer producer = testingKafkaServer.createProducer()) {
            Random random = new Random();
            IntStream.range(0, count)
                    .forEach(x -> {
                        String word = words.get(random.nextInt(totalWords));
                        producer.send(new ProducerRecord<>(topicName, word));
                        if (countMap.containsKey(word)) {
                            countMap.put(word, countMap.get(word) + 1);
                        }
                        else {
                            countMap.put(word, 1);
                        }
                    });
            return countMap;
        }
    }
}
