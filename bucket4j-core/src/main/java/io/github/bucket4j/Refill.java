/*
 *
 *   Copyright 2015-2017 Vladimir Bukhtoyarov
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.bucket4j;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

/**
 * Specifies the speed of tokens regeneration.
 */
public class Refill implements Serializable {

    private final long periodNanos;
    private final long tokens;
    private final boolean intervally;
    private final Instant firstRefillTime;
    private final long nanosPerToken;

    private Refill(long tokens, Duration period, boolean intervally, Instant firstRefillTime) {
        if (tokens <= 0) {
            throw BucketExceptions.nonPositiveRefillTokens(tokens);
        }
        this.tokens = tokens;

        if (period == null) {
            throw BucketExceptions.nullPeriod();
        }
        this.periodNanos = period.toNanos();
        if (periodNanos <= 0) {
            throw BucketExceptions.nonPositivePeriod(periodNanos);
        }
        this.intervally = intervally;
        this.firstRefillTime = firstRefillTime;

        if (periodNanos < tokens) {
            // TODO throw exception
        }
        if (periodNanos % tokens == 0) {
            nanosPerToken = periodNanos / tokens;
        } else {
            this.nanosPerToken = periodNanos / tokens + 1;
        }
    }

    /**
     * Creates refill which regenerates the tokens in greedy manner.
     * This factory method is called "smooth" because of refill created by this method will add tokens to bucket as soon as possible.
     * For example smooth refill "10 tokens per 1 second" will add 1 token per each 100 millisecond,
     * in other words refill will not wait 1 second to regenerate whole bunch of 10 tokens:
     * <pre>
     * <code>Refill.smooth(600, Duration.ofMinutes(1));</code>
     * <code>Refill.smooth(10, Duration.ofSeconds(1));</code>
     * <code>Refill.smooth(1, Duration.ofMillis(100));</code>
     * </pre>
     * The three refills above absolutely equals.
     *
     * @param tokens
     * @param period
     *
     * @return
     */
    public static Refill smooth(long tokens, Duration period) {
        return new Refill(tokens, period, false, null);
    }

    public static Refill fixedInterval(long tokens, Duration period) {
        return new Refill(tokens, period, true, null);
    }

    public static Refill fixedInterval(long tokens, Duration period, Instant timeOfFirstRefill) {
        if (timeOfFirstRefill == null) {
            // TODO throw exception
        }
        return new Refill(tokens, period, true, timeOfFirstRefill);
    }

    public long getPeriodNanos() {
        return periodNanos;
    }

    public long getTokens() {
        return tokens;
    }

    public Instant getFirstRefillTime() {
        return firstRefillTime;
    }

    public long getNanosPerToken() {
        return nanosPerToken;
    }

    @Override
    public String toString() {
        return "Refill{" +
                "periodNanos=" + periodNanos +
                ", tokens=" + tokens +
                ", intervally=" + intervally +
                ", firstRefillTime=" + firstRefillTime +
                '}';
    }

}
