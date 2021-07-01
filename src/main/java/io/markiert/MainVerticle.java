package io.markiert;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import io.markiert.gen.MarkiertClock;
import io.markiert.gen.MarkiertGenerator;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

public class MainVerticle extends AbstractVerticle {
    public final String ENV_PORT_KEY = "MARKIERT_HTTP_PORT";
    public final String ENV_DATACENTER_ID_KEY = "MARKIERT_DATACENTER_ID";
    public final String ENV_MACHINE_ID_KEY = "MARKIERT_MACHINE_ID";
    public final int DEFAULT_HTTP_PORT = 8080;
    public final int DEFAULT_DATACENTER_ID = 3;
    public final int DEFAULT_MACHINE_ID = 1;
    
    @Override
    public void start(Future<Void> fut) {
        final int httpPort = getHttpPort();

        startWebServer(fut, httpPort);
    }

    public void startWebServer(Future<Void> fut, int port) {
        Injector injector = Guice.createInjector(new AbstractModule() {
            protected void configure() {
                bind(MarkiertClock.class).to(MarkiertClock.class);
            }
        });
        MarkiertGenerator gen = injector.getInstance(MarkiertGenerator.class);

        // MarkiertClock clock = new MarkiertClock(0);
        // MarkiertGenerator gen = new MarkiertGenerator(clock, getDatacenterId(), getMachineId());

        String id = String.format("%1$" + 64 + "s", Long.toBinaryString(gen.getId())).replace(' ', '0');
        System.out.println(id);

        vertx
            .createHttpServer()
            .requestHandler(r -> {
                r.response().end("<h1>Hello from my first " +
                    "Vert.x 3 application</h1>");
            })
            .listen(port, result -> {
                if (result.succeeded()) {
                    fut.complete();
                } else {
                    fut.fail(result.cause());
                }
            });
    }

    public int getEnvInteger(String envKey, int defaultValue) {
        String envValue = System.getenv(envKey);
        if (envValue == null) {
            System.err.println("environment variable '" + envKey + "' not found, defaulting to " + defaultValue);
            return defaultValue;
        }
        try {
            return Integer.parseInt(envValue);
        } catch (NumberFormatException e) {
            System.err.println("unable to parse "  + envKey + " value of '" + "', defaulting to " + defaultValue);
            return defaultValue;
        }
    }

    public int getHttpPort() {
        return getEnvInteger(ENV_PORT_KEY, DEFAULT_HTTP_PORT);
    }

    public int getDatacenterId() {
        return getEnvInteger(ENV_DATACENTER_ID_KEY, DEFAULT_DATACENTER_ID);
    }

    public int getMachineId() {
        return getEnvInteger(ENV_MACHINE_ID_KEY, DEFAULT_MACHINE_ID);
    }
}