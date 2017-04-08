/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@JsonTypeName("created.1")
@Value.Immutable
@JsonSerialize
@JsonDeserialize(as = ImmutableRecipeCreatedEvent.class)
public interface RecipeCreatedEvent extends Event {

    CreateRecipe create();

    @Override
    default void match(Matcher matcher) {
        matcher.match(this);
    }

    static ImmutableRecipeCreatedEvent.Builder builder() {
        return ImmutableRecipeCreatedEvent.builder();
    }
}
