/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.immutables.value.Value;

@Value.Immutable
public abstract class RecipeId {

    @Value.Parameter
    protected abstract String string();

    @JsonCreator
    public static RecipeId fromString(String string) {
        return ImmutableRecipeId.of(string);
    }

    @Override
    @JsonValue
    public String toString() {
        return string();
    }
}
