/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

public interface WritableRecipeStore extends RecipeStore {

    void consume(Event event);
}
