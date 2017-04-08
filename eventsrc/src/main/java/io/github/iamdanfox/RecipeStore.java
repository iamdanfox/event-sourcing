/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RecipeStore {
    private final Map<RecipeId, String> contentsById = new HashMap<>();

    public Optional<RecipeResponse> getRecipeById(RecipeId id) {
        return Optional.ofNullable(contentsById.get(id))
                .map(contents -> {
                    return RecipeResponse.builder()
                            .id(id)
                            .contents(contents)
                            .build();
                });

    }

    public void consume(RecipeCreatedEvent event) {
        contentsById.put(event.id(), event.create().contents());
    }
}
