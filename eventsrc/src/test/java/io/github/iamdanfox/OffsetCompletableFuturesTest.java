/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package io.github.iamdanfox;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import com.google.common.base.Stopwatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class OffsetCompletableFuturesTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    OffsetCompletableFutures futures = new OffsetCompletableFutures();

    @Test
    public void if_150_already_seen_wait_for_123_returns_immediately() throws Exception {
        futures.updateMaxOffset(150L);
        Future<?> future = futures.forOffset(123L);

        Stopwatch watch = Stopwatch.createStarted();
        future.get(1, TimeUnit.SECONDS);
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), is(lessThan(100L)));
    }

    @Test
    public void forOffset_123_returns_a_future_that_is_complete_after_124_is_consumed() throws Exception {
        Future<?> future = futures.forOffset(123L);

        futures.updateMaxOffset(124L);
        future.get(1, TimeUnit.SECONDS);
    }

    @Test
    public void forOffset_123_returns_a_future_isnt_complete_by_default() throws Exception {
        Future<?> future = futures.forOffset(123L);

        futures.updateMaxOffset(122L);
        exception.expect(TimeoutException.class);

        future.get(100, TimeUnit.MILLISECONDS);
    }

}
