/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.iamdanfox.ImmutableRemoveTagEvent.Builder;
import org.immutables.value.Value;

@JsonTypeName("remove-tag.1")
@Value.Immutable
@JsonSerialize
@JsonDeserialize(as = ImmutableRemoveTagEvent.class)
public interface RemoveTagEvent extends Event {

    RecipeTag tag();

    @Override
    default void match(Matcher matcher) {
        matcher.match(this);
    }

    static Builder builder() {
        return ImmutableRemoveTagEvent.builder();
    }
}

