/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize
@JsonDeserialize(as = ImmutableCreateRecipe.class)
public interface CreateRecipe {

    String contents();

    static ImmutableCreateRecipe.Builder builder() {
        return ImmutableCreateRecipe.builder();
    }
}
