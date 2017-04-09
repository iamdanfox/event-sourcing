/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;

public interface KafkaUtils {

    static Properties properties() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        return props;
    }

    static Properties consumerProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "randomId"); //UUID.randomUUID().toString());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "client-id-test-consumer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    static KafkaConsumer<byte[], Event> jsonConsumer() {
        return new KafkaConsumer<>(
                consumerProperties(),
                new ByteArrayDeserializer(),
                new JacksonKafkaDeserializer<>(Event.class));
    }

    static KafkaProducer<byte[], Event> jsonProducer() {
        return new KafkaProducer<>(
                properties(),
                new ByteArraySerializer(),
                new JacksonKafkaSerializer<>());
    }

}
