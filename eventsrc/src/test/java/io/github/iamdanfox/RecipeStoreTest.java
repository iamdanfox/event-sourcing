/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.util.Optional;
import org.junit.Test;

public class RecipeStoreTest {

    @Test
    public void returns_empty_for_lookup_by_id_initially() {
        RecipeStore store = new RecipeStore();
        assertThat(store.getRecipeById(Literals.ID), is(Optional.empty()));
    }

    @Test
    public void created_event_makes_lookup_succeed() {
        RecipeStore store = prefilled1();
        assertThat(store.getRecipeById(Literals.ID), is(Optional.of(RecipeResponse.builder()
                .id(Literals.ID)
                .contents("recipe contents")
                .build())));
    }

    @Test
    public void add_tag_event_means_response_contains_that_tag() {
        RecipeStore store = prefilled1();

        store.consume(AddTagEvent.builder()
                .id(Literals.ID)
                .tag(Literals.TAG)
                .build());

        RecipeResponse response = store.getRecipeById(Literals.ID).get();
        assertThat(response.tags(), contains(Literals.TAG));
    }

    public static RecipeStore prefilled1() {
        RecipeStore store = new RecipeStore();
        store.consume(RecipeCreatedEvent.builder()
                .id(Literals.ID)
                .create(CreateRecipe.builder()
                        .contents("recipe contents")
                        .build())
                .build());
        return store;
    }
}
