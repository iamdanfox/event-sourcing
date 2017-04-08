/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import static com.palantir.docker.compose.logging.LogDirectory.circleAwareLogDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.ImmutableList;
import com.palantir.docker.compose.DockerComposeRule;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.StreamSupport;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.ClassRule;
import org.junit.Test;

public class KafkaIntegrationTest {

    @ClassRule
    public static final DockerComposeRule docker = DockerComposeRule.builder()
            .file("../docker-compose.yml")
            .saveLogsTo(circleAwareLogDirectory(KafkaIntegrationTest.class))
            .build();

    @Test
    public void smoke_test() throws InterruptedException, ExecutionException, TimeoutException {
        try (Producer<byte[], String> producer = new KafkaProducer<>(
                KafkaUtils.properties(),
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

        try (Producer<byte[], Event> producer = KafkaUtils.jsonProducer()) {
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

        try (Producer<byte[], Event> producer = KafkaUtils.jsonProducer()) {
            ProducerRecord<byte[], Event> record = new ProducerRecord<>("consume_something", event);
            Future<RecordMetadata> send = producer.send(record);
            RecordMetadata metadata = send.get(10, TimeUnit.SECONDS);
            assertThat(metadata.serializedValueSize(), is(63));
        }

        try (Consumer<byte[], Event> consumer = KafkaUtils.jsonConsumer()) {
            consumer.subscribe(ImmutableList.of("consume_something"));

            Event value = repeatPoll(consumer, 10);
            System.out.println(event);
            assertThat(value, is(event));
        }
    }

    private static <T> T repeatPoll(Consumer<byte[], T> consumer, int retries) {
        assertThat("Poll should succeed within specified retries", retries, is(greaterThan(0)));
        ConsumerRecords<byte[], T> poll = consumer.poll(100L);

        if (poll.count() == 0) {
            System.out.println("Polled empty records, " + (retries - 1) + " retries available...");
            return repeatPoll(consumer, retries - 1);
        }

        ConsumerRecord<byte[], T> record =
                StreamSupport.stream(poll.spliterator(), false)
                        .findFirst()
                        .get();
        System.out.println("Poll succeeded");
        return record.value();
    }

}
