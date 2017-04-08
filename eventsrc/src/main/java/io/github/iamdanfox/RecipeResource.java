/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class RecipeResource implements RecipeService {

    private final BlockingRecipeStores recipeStores;
    private final Producer<?, Event> producer;
    private final String topic;

    public RecipeResource(BlockingRecipeStores recipeStores, Producer<?, Event> producer, String topic) {
        this.recipeStores = recipeStores;
        this.producer = producer;
        this.topic = topic;
    }

    @Override
    public Optional<RecipeResponse> getRecipe(RecipeId id) {
        return recipeStores.readable().getRecipeById(id);
    }

    @Override
    public RecipeResponse createRecipe(CreateRecipe create) {
        RecipeId id = RecipeId.fromString(UUID.randomUUID().toString());
        Event value = RecipeCreatedEvent.builder()
                .id(id)
                .create(create)
                .build();
        Future<RecordMetadata> future = producer.send(new ProducerRecord<>(topic, value));

        RecordMetadata metadata;
        try {
            metadata = future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        int partition = metadata.partition();
        long offset = metadata.offset();
        RecipeStore blockingStore = recipeStores.blockingStore(partition, offset);
        return blockingStore.getRecipeById(id).get();
    }

}
