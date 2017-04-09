/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PartitionedOffsetCompletableFutures {
    private final Map<Integer, OffsetCompletableFutures> cache = new ConcurrentHashMap<>();

    public OffsetCompletableFutures forPartition(int partition) {
        return cache.computeIfAbsent(partition, PartitionedOffsetCompletableFutures::create);
    }

    private static OffsetCompletableFutures create(int partitionId) {
        return new OffsetCompletableFutures();
    }
}
