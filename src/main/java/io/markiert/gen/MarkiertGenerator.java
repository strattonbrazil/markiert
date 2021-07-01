package io.markiert.gen;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class MarkiertGenerator
{

    public final int MAX_DATACENTER_ID = 32;
    public final int MAX_MACHINE_ID = 32;

    @Inject
    private MarkiertClock _clock;

    @Inject
    @Named("dataCenterId")
    int _dataCenterId;

    @Inject
    @Named("machineId")
    int _machineId;

    private long _prevMs = -1;
    private int _seq = -1;

    public long getId() {
        long offsetMs = timestamp();
        long id = 
            timestampBits(offsetMs) | 
            dataCenterIdBits() |
            machineIdBits() |
            getSeq(offsetMs) & 4095;

        return id;
    }

    public long timestamp() {
        return _clock.getTimestampMs();
    }

    public long timestampBits(long offsetMs) {
        return (offsetMs & 2199023255551L) << 22;
    }

    public long dataCenterIdBits() {
        return (_dataCenterId & 31) << 17;
    }

    public long machineIdBits() {
        return (_machineId & 31) << 12;
    }

    public synchronized int getSeq(long offsetMs) {
        if (_prevMs != offsetMs)
            _seq = -1;
        _seq++;

        _prevMs = offsetMs;
        return _seq;
    }
}