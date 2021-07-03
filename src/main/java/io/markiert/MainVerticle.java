package io.markiert;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

import io.markiert.gen.MarkiertGenerator;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class MainVerticle extends AbstractVerticle {
    public final String ENV_PORT_KEY = "MARKIERT_HTTP_PORT";
    public final String ENV_DATACENTER_ID_KEY = "MARKIERT_DATACENTER_ID";
    public final String ENV_MACHINE_ID_KEY = "MARKIERT_MACHINE_ID";
    public final String ENV_EPOCH_OFFSET_KEY = "MARKIERT_EPOCH_OFFSET";
    public final int DEFAULT_HTTP_PORT = 8080;
    public final int DEFAULT_DATACENTER_ID = 1;
    public final int DEFAULT_MACHINE_ID = 1;
    public final long DEFAULT_EPOCH_OFFSET = 1288834974657L; // original Snowflake epoch

    @Override
    public void start(Promise<Void> startPromise) {
        final int httpPort = getHttpPort();

        startWebServer(startPromise, httpPort);
    }

    public void startWebServer(Promise<Void> startPromise, int port) {
        Injector injector = Guice.createInjector(new AbstractModule() {
            protected void configure() {
                bindConstant().annotatedWith(Names.named("offsetFromEpochMs")).to(getEpochOffset());
                bindConstant().annotatedWith(Names.named("dataCenterId")).to(getDatacenterId());
                bindConstant().annotatedWith(Names.named("machineId")).to(getMachineId());
            }
        });
        MarkiertGenerator gen = injector.getInstance(MarkiertGenerator.class);

        Router router = Router.router(vertx);
        router.route("/id").handler(ctx -> {
            JsonObject root = new JsonObject();
            root.put("id", Long.toString(gen.getId()));
            ctx.end(root.toBuffer());
        });

        vertx
            .createHttpServer()
            .requestHandler(router)
            .listen(port, result -> {
                if (result.succeeded()) {
                    startPromise.complete();
                } else {
                    startPromise.fail(result.cause());
                }
            });
    }

    public long getEnvLong(String envKey, long defaultValue) {
        String envValue = System.getenv(envKey);
        if (envValue == null) {
            System.err.println("environment variable '" + envKey + "' not found, defaulting to " + defaultValue);
            return defaultValue;
        }
        try {
            return Long.parseLong(envValue);
        } catch (NumberFormatException e) {
            System.err.println("unable to parse "  + envKey + " value of '" + "', defaulting to " + defaultValue);
            return defaultValue;
        }
    }

    public int getHttpPort() {
        return (int)getEnvLong(ENV_PORT_KEY, DEFAULT_HTTP_PORT);
    }

    public int getDatacenterId() {
        return (int)getEnvLong(ENV_DATACENTER_ID_KEY, DEFAULT_DATACENTER_ID);
    }

    public int getMachineId() {
        return (int)getEnvLong(ENV_MACHINE_ID_KEY, DEFAULT_MACHINE_ID);
    }

    public long getEpochOffset() {
        return getEnvLong(ENV_EPOCH_OFFSET_KEY, DEFAULT_EPOCH_OFFSET);
    }

}