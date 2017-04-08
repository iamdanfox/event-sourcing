/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RecipeStore {

    private final Map<RecipeId, RecipeResponse> recipeById;

    public RecipeStore() {
        recipeById = new HashMap<>();
    }

    public Optional<RecipeResponse> getRecipeById(RecipeId id) {
        return Optional.ofNullable(recipeById.get(id));
    }

    public void consume(RecipeCreatedEvent event) {
        RecipeResponse response = RecipeResponse.builder()
                .id(event.id())
                .contents(event.create().contents())
                .build();
        recipeById.put(event.id(), response);
    }
}
