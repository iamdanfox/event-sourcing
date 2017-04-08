/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import static com.palantir.docker.compose.logging.LogDirectory.circleAwareLogDirectory;
import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.palantir.docker.compose.DockerComposeRule;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.ClassRule;
import org.junit.Test;

public class KafkaProducerIntegrationTest {

    @ClassRule
    public static final DockerComposeRule docker = DockerComposeRule.builder()
            .file("../docker-compose.yml")
            .saveLogsTo(circleAwareLogDirectory(KafkaProducerIntegrationTest.class))
            .build();

    private static final Properties properties = properties();

    @Test
    public void smoke_test() throws InterruptedException, ExecutionException, TimeoutException {
        try (Producer<byte[], String> producer = new KafkaProducer<>(
                properties,
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

        try (Producer<byte[], Event> producer = new KafkaProducer<>(
                properties,
                new ByteArraySerializer(),
                new KafkaJacksonSerializer<>())) {
            ProducerRecord<byte[], Event> record = new ProducerRecord<>("send_json", event);
            Future<RecordMetadata> send = producer.send(record);
            RecordMetadata metadata = send.get(10, TimeUnit.SECONDS);
            assertThat(metadata.serializedValueSize(), is(63));
        }
    }

    private static Properties properties() {
        Properties props = new Properties();
        props.put(BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        return props;
    }

}
