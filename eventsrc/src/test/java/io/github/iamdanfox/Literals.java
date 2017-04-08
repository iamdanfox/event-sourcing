/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

public interface Literals {

    RecipeId ID = RecipeId.fromString("id");

    RecipeTag TAG = RecipeTag.fromString("my-tag");

    AddTagEvent ADD_TAG = AddTagEvent.builder()
            .id(ID)
            .tag(TAG)
            .build();

    RemoveTagEvent REMOVE_TAG = RemoveTagEvent.builder()
            .id(ID)
            .tag(TAG)
            .build();

}
