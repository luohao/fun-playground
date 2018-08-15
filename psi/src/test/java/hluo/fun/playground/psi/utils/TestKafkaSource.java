package hluo.fun.playground.psi.utils;

import com.google.common.collect.ImmutableMap;
import hluo.fun.playground.psi.testing.TestingKafkaServer;
import hluo.fun.playground.psi.testing.TestUtils;
import hluo.fun.playground.psi.testing.kvs.TestingKeyValueStore;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteBufferDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

public class TestKafkaSource
{
    private static int totalWords = 1024;
    private static int wordLength = 10;
    private static int count = 1024 * 1024;
    private static int fetchMaxBytes = 10 * 1024 * 1024;
    private static String topicName = "wordcount";

    TestingKafkaServer kafka;
    TestingKeyValueStore kvs;

    @BeforeClass
    public void setupKafka()
            throws Exception
    {
        kafka = TestingKafkaServer.createTestingKafkaServer();
        kafka.start();
        Thread.currentThread().setContextClassLoader(null);
        TestUtils.loadTestingTopic(kafka, topicName, totalWords, wordLength, count);
    }

    @AfterClass(alwaysRun = true)
    public void stopKafka()
            throws Exception
    {
        kafka.close();
        kafka = null;
    }

    @Test
    public void testFetchBatch()
    {
        checkState(kafka != null);
        Map<String, String> configs = ImmutableMap.<String, String>builder()
                .put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getConnectString())
                .put(ConsumerConfig.GROUP_ID_CONFIG, "test-kafka-source-" + System.currentTimeMillis())
                .put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, String.valueOf(fetchMaxBytes))
                .put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
                .put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
                .put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteBufferDeserializer.class.getName())
                .put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteBufferDeserializer.class.getName())
                .build();

        KafkaSource kafkaSource = new KafkaSource(configs, topicName);
        kafkaSource.fetchBatch().stream()
                .limit(10)
                .forEach(x -> {
                    byte[] payload = new byte[x.remaining()];
                    x.get(payload);
                    System.out.println(new String(payload));
                });
    }
}
