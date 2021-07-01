package io.markiert.gen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.name.Named;

import org.junit.Test;

public class MarkiertGeneratorTest {
    Injector injector;

    private MarkiertGenerator createGenerator(long fixedTime, int dataCenterId, int machineId) {
        injector = Guice.createInjector(new AbstractModule() {
            protected void configure() {
                bind(MarkiertClock.class).to(TestMarkiertClock.class);
                bind(long.class).toInstance(fixedTime); // ignored
            }

            @Provides
            @Named("dataCenterId") 
            int dataCenterId() {
                return dataCenterId;
            }

            @Provides
            @Named("machineId") 
            int machineId() {
                return machineId;
            }
        });

        return injector.getInstance(MarkiertGenerator.class);
    }

    @Test
    public void generatesTimestampBits() {
        MarkiertGenerator gen = createGenerator(7123L, 2, 3);
        assertEquals("min value", 0, gen.timestampBits(0));

        long sevenMsBits = 0B0000000000000000000000000000000000000001110000000000000000000000L;
        assertEquals("some value", sevenMsBits, gen.timestampBits(7));

        // the 42-bits of timestamp field
        long maxBits = 0B0111111111111111111111111111111111111111110000000000000000000000L;

        assertEquals("max value", maxBits, gen.timestampBits(2199023255551L));
        assertEquals("no bit overflow", maxBits, gen.timestampBits(Long.MAX_VALUE));
    }

    @Test
    public void generatesDataCenterIdBits() {
        MarkiertGenerator gen1 = createGenerator(7123L, 0, 3);
        assertEquals("min value", 0, gen1.dataCenterIdBits());

        // the 5-bits of data center id field
        long maxBits = 0B0000000000000000000000000000000000000000001111100000000000000000L;

        MarkiertGenerator gen2 = createGenerator(7123L, 31, 3);
        assertEquals("max value", maxBits, gen2.dataCenterIdBits());

        MarkiertGenerator gen3 = createGenerator(7123L, Integer.MAX_VALUE, 3);
        assertEquals("no bit overflow", maxBits, gen3.dataCenterIdBits());
    }

    @Test
    public void generatesMachineIdBits() {
        MarkiertGenerator gen1 = createGenerator(7123L, 3, 0);
        assertEquals("min value", 0, gen1.machineIdBits());

        // the 5-bits of the machine id field
        long maxBits = 0B0000000000000000000000000000000000000000000000011111000000000000L;

        MarkiertGenerator gen2 = createGenerator(7123L, 3, 31);
        assertEquals("max value", maxBits, gen2.machineIdBits());

        MarkiertGenerator gen3 = createGenerator(7123L, 3, Integer.MAX_VALUE);
        assertEquals("no bit overflow", maxBits, gen3.machineIdBits());
    }

    @Test
    public void generatesId() {
        MarkiertGenerator gen1 = new TestMarkiertGenerator(0, 0, 0);
        long zeroId = gen1.getId();
        assertEquals("zeroed id", 0L, zeroId);
        long nextId = gen1.getId();
        assertEquals("next id", 1L, nextId);
        long lastSeqId = -1;
        for (int i = 0; i < 5000; i++) {
            lastSeqId = gen1.getId();
        }
        assertTrue("sequence doesn't affect other bits", lastSeqId < 4096);
    }

    @Test
    public void generatesSequence() {
        MarkiertGenerator gen = createGenerator(7123L, 3, 9);

        assertEquals("sequence starts at zero", 0, gen.getSeq(413));
        assertEquals("sequence increments once", 1, gen.getSeq(413));
        assertEquals("sequence increments twice", 2, gen.getSeq(413));
        assertEquals("sequence resets to zero", 0, gen.getSeq(414));
    }

    private static class TestMarkiertClock extends MarkiertClock {
        public long getTimestampMs() {
            return 7123L;
        }
    }

    private static class TestMarkiertGenerator extends MarkiertGenerator {
        private long _timestamp;
        TestMarkiertGenerator(long timestamp, int dataCenterId, int machineId) {
            _timestamp = timestamp;
            _dataCenterId = dataCenterId;
            _machineId = machineId;    
        }

        public long timestamp() { return _timestamp; }
    }
}
