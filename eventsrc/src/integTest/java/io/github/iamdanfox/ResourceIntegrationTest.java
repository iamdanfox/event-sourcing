/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import static com.palantir.docker.compose.logging.LogDirectory.circleAwareLogDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.palantir.docker.compose.DockerComposeRule;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
            .saveLogsTo(circleAwareLogDirectory(ResourceIntegrationTest.class))
            .build();

    ExecutorService consumerExecutor = Executors.newSingleThreadExecutor();

    @After
    public void after() {
        consumerExecutor.shutdownNow();
    }

    @Test
    public void recipe_resource_smoke_test() throws Exception {
        try (Producer<byte[], Event> producer = KafkaUtils.jsonProducer()) {

            WritableRecipeStore underlyingStore = new InMemoryRecipeStore();
            BlockingRecipeStores recipeStores = new BlockingRecipeStores(underlyingStore);

            KafkaConsumer<byte[], Event> consumer = KafkaUtils.jsonConsumer();
            consumerExecutor.submit(() -> {
                consumer.subscribe(ImmutableList.of("resource_smoke_test"));
                while (true) {
                    ConsumerRecords<byte[], Event> poll = consumer.poll(100);
                    // System.out.println(poll.count());
                    poll.forEach(recipeStores::consume);
                }
            });

            RecipeResource resource = new RecipeResource(
                    recipeStores, producer, "resource_smoke_test");

            // sleep to let kafka / zookeeper warm up
            Thread.sleep(2000);

            benchmarkBlockingStyle(resource, 1000);
            benchmarkCallbackStyle(resource, 5000);

            consumer.wakeup();
        }
    }

    protected void benchmarkCallbackStyle(RecipeResource resource, int count) throws InterruptedException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        CountDownLatch latch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            resource.createRecipe2(CreateRecipe.builder()
                    .contents("some-contents")
                    .build(), ignured -> latch.countDown());
        }
        assertThat(latch.await(10, TimeUnit.SECONDS), is(true));
        long callbackMs = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        System.out.println("callback style:" + callbackMs + " ms for " + count + "calls");
    }

    protected void benchmarkBlockingStyle(RecipeResource resource, int count) {
        Stopwatch stopwatch2 = Stopwatch.createStarted();
        for (int i = 0; i < count; i++) {
            resource.createRecipe(CreateRecipe.builder()
                    .contents("some-contents")
                    .build());
        }
        long ms = stopwatch2.elapsed(TimeUnit.MILLISECONDS);
        System.out.println("regular:" + ms + " ms for " + count + " calls");
    }
}
