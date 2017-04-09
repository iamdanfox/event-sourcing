/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;

public class BlockingRecipeStoresTest {

    Event event = mock(Event.class);
    WritableRecipeStore underlyingStore = mock(WritableRecipeStore.class);
    BlockingRecipeStores stores = new BlockingRecipeStores(underlyingStore);
    ExecutorService testExecutor = Executors.newSingleThreadExecutor();

    @Test
    public void consuming_an_event_should_delegate_to_real_store() {
        stores.updateMaxOffsets(consumerRecord());
        verify(underlyingStore).consume(event);
    }

    @Test
    public void after_consuming_123_query_for_100_returns_immediately() throws Exception {
        stores.updateMaxOffsets(consumerRecord());
        CompletableFuture<?> blockingStore = stores.offsetLoaded(0, 100L);

        blockingStore.get(1, TimeUnit.SECONDS);
    }

    @Test
    public void after_consuming_123_query_for_124_blocks() throws Exception {
        stores.updateMaxOffsets(consumerRecord());
        CompletableFuture<?> blockingStore = stores.offsetLoaded(0, 124L);

        CountDownLatch latch = new CountDownLatch(1);
        testExecutor.submit(() -> {
            blockingStore.get(2, TimeUnit.SECONDS);
            latch.countDown();
            return null;
        });
        assertThat("latch should never be released", latch.await(100, TimeUnit.MILLISECONDS), is(false));
    }

    @Test
    public void access_to_raw_store_still_available() {
        RecipeStore instance = stores.readable();
        assertThat(instance, is(sameInstance(underlyingStore)));
    }

    private ConsumerRecord<?, Event> consumerRecord() {
        int partition = 0;
        long offset = 123L;
        Object key = null;
        ConsumerRecord<Object, Event> record = new ConsumerRecord<>("topic", partition, offset, key, event);
        return record;
    }
}
