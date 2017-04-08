/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BlockingStore implements RecipeStore {

    private final Duration timeout;
    private final RecipeStore underlyingStore;
    private final Future<?> offsetReachedFuture;

    public BlockingStore(Duration timeout, RecipeStore underlyingStore, Future<?> offsetReachedFuture) {
        this.timeout = timeout;
        this.underlyingStore = underlyingStore;
        this.offsetReachedFuture = offsetReachedFuture;
    }

    @Override
    public Optional<RecipeResponse> getRecipeById(RecipeId id) {
        return waitForOffset().getRecipeById(id);
    }

    private RecipeStore waitForOffset() {
        try {
            offsetReachedFuture.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        return underlyingStore;
    }
}
