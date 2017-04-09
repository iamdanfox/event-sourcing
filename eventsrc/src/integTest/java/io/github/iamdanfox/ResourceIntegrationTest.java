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
import java.util.Optional;
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

            WritableRecipeStore recipeStore = new InMemoryRecipeStore();
            OffsetFutures futures = new OffsetFutures();

            KafkaConsumer<byte[], Event> consumer = KafkaUtils.jsonConsumer();
            consumerExecutor.submit(() -> {
                consumer.subscribe(ImmutableList.of("resource_smoke_test"));
                while (true) {
                    ConsumerRecords<byte[], Event> poll = consumer.poll(400);
                    poll.forEach(record -> {
                        recipeStore.consume(record.value());
                        futures.updateMaxOffsets(record.partition(), record.offset());
                    });
                }
            });

            RecipeResource resource = new RecipeResource(
                    recipeStore, futures, producer, "resource_smoke_test");

            // sleep to let kafka / zookeeper warm up
            Thread.sleep(4000);

            benchmarkBlockingWrite(resource, 100);
            benchmarkAddTag(resource, 50);
            benchmarkCallbackWrite(resource, 5000);
            benchmarkReads(resource, 5000);

            consumer.wakeup();
        }
    }

    protected void benchmarkBlockingWrite(RecipeResource resource, int count) {
        Stopwatch stopwatch2 = Stopwatch.createStarted();
        for (int i = 0; i < count; i++) {
            resource.createRecipe(CreateRecipe.builder()
                    .contents("some-contents")
                    .build());
        }
        long micro = stopwatch2.elapsed(TimeUnit.MICROSECONDS);
        System.out.println("regular:" + micro / count + " microseconds per call (" + count + ")");
    }

    protected void benchmarkCallbackWrite(RecipeResource resource, int count) throws InterruptedException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        CountDownLatch latch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            resource.createRecipeAsync(CreateRecipe.builder()
                    .contents("some-contents")
                    .build()).thenAccept(ignored -> latch.countDown());
        }
        assertThat(latch.await(10, TimeUnit.SECONDS), is(true));
        long micro = stopwatch.elapsed(TimeUnit.MICROSECONDS);
        System.out.println("callback:" + micro / count + " microseconds per call (" + count + ")");
    }

    protected void benchmarkReads(RecipeResource resource, int count) {
        RecipeResponse response = resource.createRecipe(CreateRecipe.builder()
                .contents("some-contents")
                .build());

        Stopwatch stopwatch = Stopwatch.createStarted();
        Optional<RecipeResponse> answer = Optional.empty();
        for (int i = 0; i < count; i++) {
            answer = resource.getRecipe(response.id());
        }
        long nano = stopwatch.elapsed(TimeUnit.NANOSECONDS);
        assertThat(answer.get().contents(), is("some-contents"));
        System.out.println("reads:" + nano / count + " nanos per call n = " + count);
    }

    protected void benchmarkAddTag(RecipeResource resource, int count) {
        RecipeResponse response = resource.createRecipe(CreateRecipe.builder()
                .contents("some-contents")
                .build());

        Stopwatch stopwatch = Stopwatch.createStarted();
        RecipeResponse answer = null;
        for (int i = 0; i < count; i++) {
            answer = resource.addTag(response.id(), RecipeTag.fromString("my-tag-" + i));
        }
        long micros = stopwatch.elapsed(TimeUnit.MICROSECONDS);
        assertThat(answer.tags().size(), is(count));
        System.out.println("tags:" + micros / count + " microseconds per call n = " + count);
    }
}
