/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class PartitionedOffsetCompletableFutures {

    private final Map<Integer, OffsetCompletableFutures> cache = new ConcurrentHashMap<>();
    private static final Function<Integer, OffsetCompletableFutures> FACTORY =
            partitionId -> new OffsetCompletableFutures();

    public OffsetCompletableFutures forPartition(int partition) {
        return cache.computeIfAbsent(partition, FACTORY);
    }
}
