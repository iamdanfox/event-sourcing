/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import static java.util.stream.Collectors.toList;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public class OffsetCompletableFutures {

    private AtomicLong maxOffsetSeenAlready = new AtomicLong(-1);
    private final Set<BlockingCondition> listeners = Sets.newConcurrentHashSet();

    public CompletableFuture<?> futureForOffset(long waitForOffset) {
        if (maxOffsetSeenAlready.get() >= waitForOffset) {
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
        maxOffsetSeenAlready.accumulateAndGet(offset, Math::max);
        List<BlockingCondition> forRemoval = listeners.stream()
                .filter(blockingCondition -> blockingCondition.satisfiedByOffset(offset))
                .collect(toList());
        forRemoval.forEach(listeners::remove);
    }

    private interface BlockingCondition {
        boolean satisfiedByOffset(long newMaxOffset);
    }
}
