package io.markiert.gen;

import static org.junit.Assert.assertEquals;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.Before;
import org.junit.Test;

public class MarkiertClockTest {
    Injector injector;

    @Before
    public void setup() {
        injector = Guice.createInjector(new AbstractModule() {
            protected void configure() {
                bind(SystemClock.class).to(TestSystemClock.class);
                bind(long.class).toInstance(1000L);
            }
        });
    }

    @Test
    public void testClock() {
        MarkiertClock clock = injector.getInstance(MarkiertClock.class);
        long ts = clock.getTimestampMs();
        assertEquals("provides milliseconds from reference", ts, 6500L);
        ts = clock.getTimestampMs();
        assertEquals("provides milliseconds from reference after time passed", ts, 7000L);
    }

    private static class TestSystemClock extends SystemClock {
        private long currentTimeMs = 7000L; // 7 seconds after UNIX epoch
        public long currentTimeMillis() {
            currentTimeMs += 500;
            return currentTimeMs;
        }
    }
}