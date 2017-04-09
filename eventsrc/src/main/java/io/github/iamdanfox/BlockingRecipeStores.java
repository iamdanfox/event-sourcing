/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public class BlockingRecipeStores {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final WritableRecipeStore underlyingStore;
    private final PartitionedOffsetCompletableFutures completables;

    public BlockingRecipeStores(WritableRecipeStore underlyingStore) {
        this.underlyingStore = underlyingStore;
        this.completables = new PartitionedOffsetCompletableFutures();
    }

    public void updateMaxOffsets(ConsumerRecord<?, Event> record) {
        underlyingStore.consume(record.value());
        completables.forPartition(record.partition()).updateMaxOffset(record.offset());
    }

    public CompletableFuture<?> offsetLoaded(int partition, long offset) {
        return completables.forPartition(partition).forOffset(offset);
    }

    /**
     * Get the underlying {@link RecipeStore} that will return answers immediately.
     */
    public RecipeStore readable() {
        return underlyingStore;
    }

}
