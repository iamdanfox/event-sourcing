/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import java.util.concurrent.CompletableFuture;

public class OffsetFutures {

    private final PartitionedOffsetCompletableFutures completables;

    public OffsetFutures() {
        this.completables = new PartitionedOffsetCompletableFutures();
    }

    public void updateMaxOffsets(int partition, long offset) {
        completables.forPartition(partition).updateMaxOffset(offset);
    }

    public CompletableFuture<?> offsetLoaded(int partition, long offset) {
        return completables.forPartition(partition).futureForOffset(offset);
    }

}
