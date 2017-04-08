/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class PartitionedOffsetCompletableFuturesTest {

    PartitionedOffsetCompletableFutures futures = new PartitionedOffsetCompletableFutures();

    @Test
    public void same_instances_returned() {
        OffsetCompletableFutures first = futures.forPartition(0);
        OffsetCompletableFutures second = futures.forPartition(0);
        assertThat(first, is(sameInstance(second)));
    }

    @Test
    public void different_instances() {
        OffsetCompletableFutures first = futures.forPartition(0);
        OffsetCompletableFutures second = futures.forPartition(1);
        assertThat(first, is(not(sameInstance(second))));
    }
}
