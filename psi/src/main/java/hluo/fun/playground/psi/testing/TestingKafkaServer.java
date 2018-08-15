package hluo.fun.playground.psi.testing;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import kafka.admin.TopicCommand;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.security.Permission;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.io.MoreFiles.deleteRecursively;
import static com.google.common.io.RecursiveDeleteOption.ALLOW_INSECURE;
import static hluo.fun.playground.psi.testing.TestUtils.findUnusedPort;
import static hluo.fun.playground.psi.testing.TestUtils.toProperties;
import static java.util.Objects.requireNonNull;

public class TestingKafkaServer
        implements Closeable
{
    private final EmbeddedZookeeper zookeeper;
    private final int port;
    private final File kafkaDataDir;
    private final KafkaServerStartable kafka;

    private final AtomicBoolean started = new AtomicBoolean();
    private final AtomicBoolean stopped = new AtomicBoolean();

    public static TestingKafkaServer createTestingKafkaServer()
            throws IOException
    {
        return new TestingKafkaServer(new EmbeddedZookeeper(), new Properties());
    }

    public static TestingKafkaServer createTestingKafkaServer(Properties overrideProperties)
            throws IOException
    {
        return new TestingKafkaServer(new EmbeddedZookeeper(), overrideProperties);
    }

    TestingKafkaServer(EmbeddedZookeeper zookeeper, Properties overrideProperties)
            throws IOException
    {
        this.zookeeper = requireNonNull(zookeeper, "zookeeper is null");
        requireNonNull(overrideProperties, "overrideProperties is null");

        this.port = findUnusedPort();
        this.kafkaDataDir = Files.createTempDir();

        Map<String, String> properties = ImmutableMap.<String, String>builder()
                .put("broker.id", "0")
                .put("host.name", "localhost")
                .put("num.partitions", "2")
                .put("log.flush.interval.messages", "10000")
                .put("offsets.topic.replication.factor", "1")
                .put("log.flush.interval.ms", "1000")
                .put("log.retention.minutes", "60")
                .put("log.segment.bytes", "1048576")
                .put("auto.create.topics.enable", "false")
                .put("zookeeper.connection.timeout.ms", "1000000")
                .put("port", Integer.toString(port))
                .put("log.dirs", kafkaDataDir.getAbsolutePath())
                .put("zookeeper.connect", zookeeper.getConnectString())
                .putAll(Maps.fromProperties(overrideProperties))
                .build();

        KafkaConfig config = new KafkaConfig(toProperties(properties));
        this.kafka = new KafkaServerStartable(config);
    }

    public void start()
            throws InterruptedException, IOException
    {
        if (!started.getAndSet(true)) {
            zookeeper.start();
            kafka.startup();
        }
    }

    @Override
    public void close()
            throws IOException
    {
        if (started.get() && !stopped.getAndSet(true)) {
            kafka.shutdown();
            kafka.awaitShutdown();
            zookeeper.close();
            deleteRecursively(kafkaDataDir.toPath(), ALLOW_INSECURE);
        }
    }

    public void createTopics(String... topics)
    {
        createTopics(2, 1, new Properties(), topics);
    }

    public void createTopics(int partitions, int replication, Properties topicProperties, String... topics)
    {
        checkState(started.get() && !stopped.get(), "not started!");

        for (String topic : topics) {
            String[] topicArgs = new String[] {
                    "--create",
                    "--zookeeper", getZookeeperConnectString(),
                    "--replication-factor", String.valueOf(replication),
                    "--partitions", String.valueOf(partitions),
                    "--topic", topic
            };

            invokeTopicCommand(topicArgs);
        }
    }

    public CloseableProducer<String, String> createProducer()
    {
        Map<String, String> properties = ImmutableMap.<String, String>builder()
                .put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getConnectString())
                .put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName())
                .put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName())
                .put(ProducerConfig.ACKS_CONFIG, "1")
                .build();

        return new CloseableProducer<>(toProperties(properties));
    }

    public static class CloseableProducer<K, V>
            extends KafkaProducer<K, V>
            implements AutoCloseable
    {
        public CloseableProducer(Properties properties)
        {
            super(properties);
        }
    }

    public int getZookeeperPort()
    {
        return zookeeper.getPort();
    }

    public int getPort()
    {
        return port;
    }

    public String getConnectString()
    {
        return "localhost:" + Integer.toString(port);
    }

    public String getZookeeperConnectString()
    {
        return zookeeper.getConnectString();
    }

    private static void invokeTopicCommand(String[] args) {
        // jfim: Use Java security to trap System.exit in Kafka 0.9's TopicCommand
        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkPermission(Permission perm) {
                if (perm.getName().startsWith("exitVM")) {
                    throw new SecurityException("System.exit is disabled");
                }
            }

            @Override
            public void checkPermission(Permission perm, Object context) {
                checkPermission(perm);
            }
        });

        try {
            TopicCommand.main(args);
        } catch (SecurityException ex) {
            // Do nothing, this is caused by our security manager that disables System.exit
        }

        System.setSecurityManager(null);
    }
}
