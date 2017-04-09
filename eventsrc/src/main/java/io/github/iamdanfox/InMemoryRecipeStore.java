/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.github.iamdanfox.Event.Matcher;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryRecipeStore implements Matcher, WritableRecipeStore {

    private final Map<RecipeId, String> contentsById = new HashMap<>();
    private final Multimap<RecipeId, RecipeTag> tagsById = HashMultimap.create();

    @Override
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

    @Override
    public void match(RecipeCreatedEvent event) {
        contentsById.put(event.id(), event.create().contents());
    }

    @Override
    public void match(AddTagEvent event) {
        tagsById.put(event.id(), event.tag());
    }

    @Override
    public void match(RemoveTagEvent event) {
        tagsById.remove(event.id(), event.tag());
    }

    @Override
    public void consume(Event event) {
        event.match(this);
    }

}
