/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import static com.palantir.docker.compose.logging.LogDirectory.circleAwareLogDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.ImmutableList;
import com.palantir.docker.compose.DockerComposeRule;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.StreamSupport;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.ClassRule;
import org.junit.Test;

public class KafkaProducerIntegrationTest {

    @ClassRule
    public static final DockerComposeRule docker = DockerComposeRule.builder()
            .file("../docker-compose.yml")
            .saveLogsTo(circleAwareLogDirectory(KafkaProducerIntegrationTest.class))
            .build();

    @Test
    public void smoke_test() throws InterruptedException, ExecutionException, TimeoutException {
        try (Producer<byte[], String> producer = new KafkaProducer<>(
                properties(),
                new ByteArraySerializer(),
                new StringSerializer())) {
            ProducerRecord<byte[], String> record = new ProducerRecord<>("smoke_test", "value");
            Future<RecordMetadata> send = producer.send(record);
            RecordMetadata metadata = send.get(10, TimeUnit.SECONDS);
            assertThat(metadata.offset(), is(0L));
        }
    }

    @Test
    public void send_json() throws InterruptedException, ExecutionException, TimeoutException {
        Event event = ImmutableRecipeCreatedEvent.builder()
                .id(RecipeId.fromString("id"))
                .create(ImmutableCreateRecipe.builder()
                        .contents("contents")
                        .build())
                .build();

        try (Producer<byte[], Event> producer = jsonProducer()) {
            ProducerRecord<byte[], Event> record = new ProducerRecord<>("send_json", event);
            Future<RecordMetadata> send = producer.send(record);
            RecordMetadata metadata = send.get(10, TimeUnit.SECONDS);
            assertThat(metadata.serializedValueSize(), is(63));
        }
    }

    @Test
    public void consume_something() throws InterruptedException, ExecutionException, TimeoutException {
        Event event = ImmutableRecipeCreatedEvent.builder()
                .id(RecipeId.fromString("id"))
                .create(ImmutableCreateRecipe.builder()
                        .contents("contents")
                        .build())
                .build();

        try (Producer<byte[], Event> producer = jsonProducer()) {
            ProducerRecord<byte[], Event> record = new ProducerRecord<>("consume_something", event);
            Future<RecordMetadata> send = producer.send(record);
            RecordMetadata metadata = send.get(10, TimeUnit.SECONDS);
            assertThat(metadata.serializedValueSize(), is(63));
        }

        try (Consumer<byte[], String> consumer = new KafkaConsumer<>(
                consumerProperties(),
                new ByteArrayDeserializer(),
                new StringDeserializer())) {
            consumer.subscribe(ImmutableList.of("consume_something"));

            String value = repeatPoll(consumer, 10);
            assertThat(value, containsString("contents"));
        }

    }

    protected String repeatPoll(Consumer<byte[], String> consumer, int retries) {
        assertThat("Poll should succeed with specified retries", retries, is(greaterThan(0)));
        ConsumerRecords<byte[], String> poll = consumer.poll(100L);

        if (poll.count() == 0) {
            System.out.println("Polled empty records, " + (retries - 1) + " retries available...");
            return repeatPoll(consumer, retries - 1);
        }

        ConsumerRecord<byte[], String> record =
                StreamSupport.stream(poll.spliterator(), false)
                        .findFirst()
                        .get();
        System.out.println("Poll succeeded");
        return record.value();
    }

    private static KafkaProducer<byte[], Event> jsonProducer() {
        return new KafkaProducer<>(
                properties(),
                new ByteArraySerializer(),
                new KafkaJacksonSerializer<>());
    }

    private static Properties properties() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        return props;
    }

    private static Properties consumerProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "randomId"); //UUID.randomUUID().toString());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "client-id-test-consumer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

}
