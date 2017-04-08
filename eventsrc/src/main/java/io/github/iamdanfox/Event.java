/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(RecipeCreatedEvent.class),
        @JsonSubTypes.Type(AddTagEvent.class),
        @JsonSubTypes.Type(RemoveTagEvent.class)
        })
public interface Event {

    RecipeId id();

    void match(Matcher matcher);

    interface Matcher {
        void match(RecipeCreatedEvent event);
        void match(AddTagEvent event);
        void match(RemoveTagEvent event);
    }
}
