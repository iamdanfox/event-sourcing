/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/")
public interface RecipeService {

    @GET
    @Path("recipes/:id")
    RecipeResponse getRecipe(@PathParam("id") RecipeId id);

    @POST
    @Path("recipes")
    RecipeResponse createRecipe(CreateRecipe create);
}
