package hluo.fun.playground.psi.utils;

import hluo.fun.playground.psi.testing.TestUtils;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Objects.requireNonNull;

/**
 * An example of Kafka source
 */
public class KafkaSource
{
    private KafkaConsumer<ByteBuffer, ByteBuffer> consumer;

    public KafkaSource(Map<String, String> config, String topicName)
    {
        requireNonNull(config.get("bootstrap.servers"), "bootstrap.servers is null");
        requireNonNull(config.get("group.id"), "group.id is null");
        requireNonNull(config.get("key.deserializer"), "key.deserializer is null");
        requireNonNull(config.get("value.deserializer"), "value.deserializer is null");

        this.consumer = new KafkaConsumer<>(TestUtils.toProperties(config));

        List<TopicPartition> partitions = consumer.partitionsFor(topicName).stream()
                .map(x -> new TopicPartition(topicName, x.partition()))
                .collect(toImmutableList());

        consumer.assign(partitions);
        // FIXME: partition the dataset
    }

    public List<ByteBuffer> fetchBatch()
    {
        List<ByteBuffer> batch = new ArrayList<>();
        consumer.poll(1000)
                .forEach(x -> batch.add(x.value()));
        return batch;
    }
}
