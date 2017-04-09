/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class RecipeResource implements RecipeService {

    private final OffsetFutures offsetFutures;
    private final Producer<?, Event> producer;
    private final String topic;
    private final RecipeStore recipeStore;

    public RecipeResource(
            RecipeStore recipeStore,
            OffsetFutures offsetFutures,
            Producer<?, Event> producer,
            String topic) {
        this.recipeStore = recipeStore;
        this.offsetFutures = offsetFutures;
        this.producer = producer;
        this.topic = topic;
    }

    @Override
    public Optional<RecipeResponse> getRecipe(RecipeId id) {
        return recipeStore.getRecipeById(id);
    }

    @Override
    public RecipeResponse createRecipe(CreateRecipe create) {
        RecipeId id = RecipeId.fromString(UUID.randomUUID().toString());
        Event value = RecipeCreatedEvent.builder()
                .id(id)
                .create(create)
                .build();
        Future<RecordMetadata> future = producer.send(new ProducerRecord<>(topic, value));

        block(future);
        return recipeStore.getRecipeById(id).get();
    }

    @Override
    public RecipeResponse addTag(RecipeId id, RecipeTag tag) {
        Event value = AddTagEvent.builder()
                .id(id)
                .tag(tag)
                .build();

        block(producer.send(new ProducerRecord<>(topic, value)));
        return recipeStore.getRecipeById(id).get();
    }

    @Override
    public RecipeResponse removeTag(RecipeId id, RecipeTag tag) {
        Event value = RemoveTagEvent.builder()
                .id(id)
                .tag(tag)
                .build();

        block(producer.send(new ProducerRecord<>(topic, value)));
        return recipeStore.getRecipeById(id).get();
    }

    protected void block(Future<RecordMetadata> future) {
        RecordMetadata metadata;
        try {
            metadata = future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }

        int partition = metadata.partition();
        long offset = metadata.offset();
        CompletableFuture<?> offsetLoadedFuture = offsetFutures.offsetLoaded(partition, offset);

        try {
            offsetLoadedFuture.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public void createRecipeAsync(CreateRecipe create, Consumer<RecipeResponse> userCallback) {
        RecipeId id = RecipeId.fromString(UUID.randomUUID().toString());
        Event value = RecipeCreatedEvent.builder()
                .id(id)
                .create(create)
                .build();

        Callback kafkaCallback = (metadata, exception) -> {
            int partition = metadata.partition();
            long offset = metadata.offset();
            offsetFutures.offsetLoaded(partition, offset).thenRun(() -> {
                RecipeResponse response = recipeStore.getRecipeById(id).get();
                userCallback.accept(response);
            });
        };

        producer.send(new ProducerRecord<>(topic, value), kafkaCallback);
    }

}
