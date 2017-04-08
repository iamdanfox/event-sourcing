/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class RecipeCreatedEventTest {

    @Test
    public void round_trip() throws Exception {
        RecipeCreatedEvent event = ImmutableRecipeCreatedEvent.builder()
                .id(RecipeId.fromString("some-id"))
                .create(ImmutableCreateRecipe.builder().contents("my recipe").build())
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String string = mapper.writeValueAsString(event);
        assertThat(mapper.readValue(string, RecipeCreatedEvent.class), is(event));
    }
}
