package io.markiert.gen;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class MarkiertClock
{
    @Inject
    private SystemClock _clock;

    @Inject
    @Named("offsetFromEpochMs")
    private Long _offsetFromEpochMs;

    public long getTimestampMs() {
        long sinceEpoch = _clock.currentTimeMillis();
        return sinceEpoch - _offsetFromEpochMs;
    }
}   