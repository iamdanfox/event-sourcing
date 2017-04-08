/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Optional;
import org.junit.Test;

public class RecipeStoreTest {

    @Test
    public void returns_empty_for_lookup_by_id_initially() {
        RecipeStore store = new RecipeStore();
        assertThat(store.getRecipeById(RecipeId.fromString("id")), is(Optional.empty()));
    }
}
