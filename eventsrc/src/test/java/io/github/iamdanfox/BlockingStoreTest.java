/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Stopwatch;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class BlockingStoreTest {

    RecipeStore underlyingStore = mock(RecipeStore.class);
    ExecutorService testExecutor = Executors.newSingleThreadExecutor();

    @Test
    public void if_future_is_done_should_delegate_call() throws InterruptedException {
        CompletableFuture<?> offsetReachedFuture = new CompletableFuture<>();
        offsetReachedFuture.complete(null);
        BlockingStore store = new BlockingStore(Duration.ofHours(1), underlyingStore, offsetReachedFuture);

        Optional<RecipeResponse> expected = Optional.of(Literals.RECIPE_RESPONSE);
        when(underlyingStore.getRecipeById(Literals.ID)).thenReturn(expected);

        assertThat(store.getRecipeById(Literals.ID), is(expected));
    }

    @Test
    public void call_should_block_for_timeout() throws InterruptedException {
        CompletableFuture<?> offsetReachedFuture = new CompletableFuture<>();
        BlockingStore store = new BlockingStore(
                Duration.ofMillis(90),
                underlyingStore,
                offsetReachedFuture);

        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            store.getRecipeById(Literals.ID);
        } catch (Exception e) {
            // expected
        }
        assertThat(stopwatch.elapsed(TimeUnit.MILLISECONDS), greaterThan(89L));
    }

    @Test
    public void call_should_block_until_future_is_done() throws InterruptedException {
        CompletableFuture<?> offsetReachedFuture = new CompletableFuture<>();
        BlockingStore store = new BlockingStore(Duration.ofHours(1), underlyingStore, offsetReachedFuture);

        CountDownLatch asyncGuarantee = new CountDownLatch(1);
        CountDownLatch completed = new CountDownLatch(1);

        testExecutor.submit(() -> {
            asyncGuarantee.await();
            store.getRecipeById(Literals.ID);
            completed.countDown();
            return null;
        });

        asyncGuarantee.countDown();
        offsetReachedFuture.complete(null);

        assertThat("Runnable should release latch", completed.await(1, TimeUnit.SECONDS), is(true));
    }
}
