/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.immutables.value.Value;

@Value.Immutable
public interface RecipeTag {

    @JsonValue
    String string();

    @JsonCreator
    static RecipeTag fromString(String string) {
        return ImmutableRecipeTag.builder().string(string).build();
    }
}
