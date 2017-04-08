/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class RecipeResonseTest {

    @Test
    public void round_trip_serialization() throws Exception {
        RecipeResponse original = ImmutableRecipeResponse.builder()
                .id(RecipeId.fromString("some-id"))
                .contents("my recipe")
                .addTags(RecipeTag.fromString("tag"))
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String string = mapper.writeValueAsString(original);
        assertThat(mapper.readValue(string, RecipeResponse.class), is(original));
    }
}
