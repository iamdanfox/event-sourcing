/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class CreateRecipeTest {

    @Test
    public void round_trip() throws Exception {
        CreateRecipe original = ImmutableCreateRecipe.builder().contents("my recipe").build();
        ObjectMapper mapper = new ObjectMapper();
        String string = mapper.writeValueAsString(original);
        assertThat(mapper.readValue(string, CreateRecipe.class), is(original));
    }
}
