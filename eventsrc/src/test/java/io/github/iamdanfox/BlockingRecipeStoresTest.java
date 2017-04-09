/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class BlockingRecipeStoresTest {

    Event event = mock(Event.class);
    WritableRecipeStore underlyingStore = mock(WritableRecipeStore.class);
    OffsetFutures stores = new OffsetFutures();
    ExecutorService testExecutor = Executors.newSingleThreadExecutor();

    @Test
    public void after_consuming_123_query_for_100_returns_immediately() throws Exception {
        stores.updateMaxOffsets(0, 123L);
        CompletableFuture<?> blockingStore = stores.offsetLoaded(0, 100L);

        blockingStore.get(1, TimeUnit.SECONDS);
    }

    @Test
    public void after_consuming_123_query_for_124_blocks() throws Exception {
        stores.updateMaxOffsets(0, 123L);
        CompletableFuture<?> blockingStore = stores.offsetLoaded(0, 124L);

        CountDownLatch latch = new CountDownLatch(1);
        testExecutor.submit(() -> {
            blockingStore.get(2, TimeUnit.SECONDS);
            latch.countDown();
            return null;
        });
        assertThat("latch should never be released", latch.await(100, TimeUnit.MILLISECONDS), is(false));
    }
}
