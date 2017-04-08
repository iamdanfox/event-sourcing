/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import java.time.Duration;
import java.util.concurrent.Future;
import java.util.function.Function;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public class BlockingStores {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final WritableRecipeStore underlyingStore;
    private final PartitionedOffsetCompletableFutures completables;
    private final Function<Future<?>, BlockingStore> createBlockingStore;

    public BlockingStores(WritableRecipeStore underlyingStore) {
        this.underlyingStore = underlyingStore;
        this.completables = new PartitionedOffsetCompletableFutures();
        this.createBlockingStore = future -> new BlockingStore(TIMEOUT, underlyingStore, future);
    }

    public void consume(ConsumerRecord<?, Event> record) {
        underlyingStore.consume(record.value());
        completables.forPartition(record.partition()).updateMaxOffset(record.offset());
    }

    public RecipeStore blockingStore(int partition, long offset) {
        Future<?> completable = completables.forPartition(partition).forOffset(offset);
        return createBlockingStore.apply(completable);
    }

}
