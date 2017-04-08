/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import static java.util.stream.Collectors.toList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class OffsetCompletableFutures {

    private long maxOffsetSeenAlready = 0;
    private final Set<BlockingCondition> listeners = new HashSet<>();

    public Future<?> forOffset(long waitForOffset) {
        if (maxOffsetSeenAlready >= waitForOffset) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> future = new CompletableFuture<Void>();
        registerBlockingCondition(waitForOffset, future);

        return future;
    }

    private void registerBlockingCondition(long waitForOffset, CompletableFuture<Void> future) {
        BlockingCondition blockingCondition = newMaxOffset -> {
            if (newMaxOffset >= waitForOffset) {
                future.complete(null);
                return true;
            }
            return false;
        };
        listeners.add(blockingCondition);
    }

    public void updateMaxOffset(long offset) {
        maxOffsetSeenAlready = Math.max(offset, maxOffsetSeenAlready);
        List<BlockingCondition> forRemoval = listeners.stream()
                .filter(blockingCondition -> blockingCondition.satisfiedByOffset(offset))
                .collect(toList());
        forRemoval.forEach(listeners::remove);
    }

    private interface BlockingCondition {
        boolean satisfiedByOffset(long newMaxOffset);
    }
}
