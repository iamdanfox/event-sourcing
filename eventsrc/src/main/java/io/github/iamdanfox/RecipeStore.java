/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RecipeStore {

    private final Map<RecipeId, String> contentsById = new HashMap<>();
    private final Multimap<RecipeId, RecipeTag> tagsById = HashMultimap.create();

    public Optional<RecipeResponse> getRecipeById(RecipeId id) {
        String contents = contentsById.get(id);

        if (contents == null) {
            return Optional.empty();
        }

        Collection<RecipeTag> tags = tagsById.get(id);

        RecipeResponse response = RecipeResponse.builder()
                .id(id)
                .contents(contents)
                .tags(tags)
                .build();

        return Optional.of(response);
    }

    public void consume(RecipeCreatedEvent event) {
        contentsById.put(event.id(), event.create().contents());
    }

    public void consume(AddTagEvent event) {
        tagsById.put(event.id(), event.tag());
    }

    public void consume(RemoveTagEvent event) {
        tagsById.remove(event.id(), event.tag());
    }
}
