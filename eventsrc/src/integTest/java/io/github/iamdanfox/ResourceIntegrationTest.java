/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import static com.palantir.docker.compose.logging.LogDirectory.circleAwareLogDirectory;

import com.google.common.collect.ImmutableList;
import com.palantir.docker.compose.DockerComposeRule;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Producer;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;

public class ResourceIntegrationTest {

    @ClassRule
    public static final DockerComposeRule docker = DockerComposeRule.builder()
            .file("../docker-compose.yml")
            .saveLogsTo(circleAwareLogDirectory(KafkaIntegrationTest.class))
            .build();

    ExecutorService executor = Executors.newSingleThreadExecutor();

    @After
    public void after() {
        executor.shutdownNow();
    }

    @Test
    public void recipe_resource_smoke_test() {
        try (Producer<byte[], Event> producer = KafkaUtils.jsonProducer()) {

            WritableRecipeStore underlyingStore = new InMemoryRecipeStore();
            BlockingRecipeStores recipeStores = new BlockingRecipeStores(underlyingStore);

            KafkaConsumer<byte[], Event> consumer = KafkaUtils.jsonConsumer();
            executor.submit(() -> {
                consumer.subscribe(ImmutableList.of("resource_smoke_test"));
                while (true) {
                    ConsumerRecords<byte[], Event> poll = consumer.poll(100);
                    System.out.println(poll.count());
                    poll.forEach(recipeStores::consume);
                }
            });

            RecipeResource resource = new RecipeResource(recipeStores, producer, "resource_smoke_test");

            RecipeResponse answer = resource.createRecipe(CreateRecipe.builder()
                    .contents("some-contents")
                    .build());

            System.out.println(answer);
            consumer.wakeup();
        }
    }
}
