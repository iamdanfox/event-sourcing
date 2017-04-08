/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;

public class OffsetTrackerTest {
    Event event = mock(Event.class);

    @Test
    public void consuming_an_event_should_delegate_to_real_store() {
        RecipeStore underlyingStore = mock(RecipeStore.class);
        OffsetTracker tracker = new OffsetTracker(underlyingStore);

        tracker.consume(consumerRecord());
        verify(underlyingStore).consume(event);
    }

    private ConsumerRecord<?, Event> consumerRecord() {
        int partition = 0;
        long offset = 123L;
        Object key = null;
        ConsumerRecord<Object, Event> record = new ConsumerRecord<>("topic", partition, offset, key, event);
        return record;
    }
}
