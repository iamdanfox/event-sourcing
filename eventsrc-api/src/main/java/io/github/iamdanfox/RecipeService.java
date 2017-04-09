/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/")
public interface RecipeService {

    @GET
    @Path("recipes/:id")
    Optional<RecipeResponse> getRecipe(@PathParam("id") RecipeId id);

    @POST
    @Path("recipes")
    RecipeResponse createRecipe(CreateRecipe create) throws InterruptedException, ExecutionException, TimeoutException;

    @PUT
    @Path("recipes/:id/tags/:tag")
    RecipeResponse addTag(@PathParam("id") RecipeId id, @PathParam("tag") RecipeTag tag);

    @DELETE
    @Path("recipes/:id/tags/:tag")
    RecipeResponse removeTag(@PathParam("id") RecipeId id, @PathParam("tag") RecipeTag tag);
}
