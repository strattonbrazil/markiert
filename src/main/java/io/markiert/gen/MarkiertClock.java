package io.markiert.gen;

import com.google.inject.Inject;

public class MarkiertClock
{
    @Inject
    private SystemClock _clock;

    @Inject
    private long _offsetFromEpochMs;

    public long getTimestampMs() {
        long sinceEpoch = _clock.currentTimeMillis();
        return sinceEpoch - _offsetFromEpochMs;
    }
}   