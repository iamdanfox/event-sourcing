/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.Optional;
import org.junit.Test;

public class RecipeStoreTest {

    private RecipeStore store = new RecipeStore();

    @Test
    public void returns_empty_for_lookup_by_id_initially() {
        assertThat(store.getRecipeById(RecipeId.fromString("id")), is(Optional.empty()));
    }

    @Test
    public void created_event_makes_lookup_succeed() {
        store.consume(RecipeCreatedEvent.builder()
                .id(RecipeId.fromString("id"))
                .create(CreateRecipe.builder()
                        .contents("recipe contents")
                        .build())
                .build());

        assertThat(store.getRecipeById(RecipeId.fromString("id")), is(not(Optional.empty())));
    }
}
