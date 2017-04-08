/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public class OffsetTracker {

    private final WritableRecipeStore underlyingStore;

    public OffsetTracker(WritableRecipeStore underlyingStore) {
        this.underlyingStore = underlyingStore;
    }

    public void consume(ConsumerRecord<?, Event> record) {
        underlyingStore.consume(record.value());
    }

    public RecipeStore blockingStore(int partition, long offset) {
        return underlyingStore;
    }

}
