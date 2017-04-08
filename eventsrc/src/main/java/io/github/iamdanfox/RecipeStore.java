/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import java.util.Optional;

public interface RecipeStore {

    Optional<RecipeResponse> getRecipeById(RecipeId id);

}
