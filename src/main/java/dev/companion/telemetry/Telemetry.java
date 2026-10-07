package dev.companion.telemetry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Anonymous, opt-out usage telemetry (PostHog).
 *
 * <p>Sends only: a random install ID, mod/game/loader/Java versions, OS name/arch, RAM, CPU cores,
 * game language, launch/session timing and error stack traces that involve this mod.
 * No usernames, UUIDs, IPs (GeoIP disabled), server addresses, chat or world data.
 *
 * <p>Disable: set "enabled": false in config/kabildzon-telemetry.json.
 */
public final class Telemetry {
    // ---- Fill in from your PostHog project settings (Project API key is write-only, safe to ship) ----
    public static final String POSTHOG_HOST = "https://us.i.posthog.com";
    public static final String POSTHOG_PROJECT_KEY = "phc_qnBV3owG8LfyLGsx4WUWYuy6Q6gxBWf2b3gDRyG2PujX";

    private static final Logger LOG = LoggerFactory.getLogger("kabildzon-telemetry");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private static final long SESSION_START = System.currentTimeMillis();

    private static Config config;
    private static boolean firstLaunch;

    private Telemetry() {
    }

    static final class Config {
        boolean enabled = true;
        String installId = UUID.randomUUID().toString();
        boolean noticeShown = false;
        String _info = "Kabildzon sends anonymous usage stats (launches, versions, OS, session length, mod errors). "
                + "No personal data. Set enabled to false to turn it off.";
    }

    public static synchronized void init() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve("kabildzon-telemetry.json");
        try {
            if (Files.exists(file)) {
                config = GSON.fromJson(Files.readString(file), Config.class);
            }
        } catch (Exception e) {
            LOG.warn("Could not read telemetry config, recreating", e);
        }
        if (config == null || config.installId == null) {
            config = new Config();
            firstLaunch = true;
        }
        save();
        if (!isEnabled()) {
            return;
        }
        installCrashHandler();
        JsonObject p = baseProperties();
        p.addProperty("first_launch", firstLaunch);
        capture("mod_launched", p, false);
    }

    public static boolean isEnabled() {
        return config != null && config.enabled && !POSTHOG_PROJECT_KEY.startsWith("phc_REPLACE");
    }

    /** True once per install: used to show the in-game notice. */
    public static synchronized boolean consumeNotice() {
        if (config == null || config.noticeShown) {
            return false;
        }
        config.noticeShown = true;
        save();
        return true;
    }

    public static void worldJoined(boolean singleplayer) {
        JsonObject p = new JsonObject();
        p.addProperty("singleplayer", singleplayer);
        p.addProperty("seconds_since_launch", (System.currentTimeMillis() - SESSION_START) / 1000);
        capture("world_joined", p, false);
    }

    public static void sessionEnded(long playSeconds, int worldsJoined) {
        JsonObject p = new JsonObject();
        p.addProperty("session_seconds", (System.currentTimeMillis() - SESSION_START) / 1000);
        p.addProperty("play_seconds", playSeconds);
        p.addProperty("worlds_joined", worldsJoined);
        capture("session_ended", p, true);
    }

    /** Reports an error only if the stack trace involves this mod. */
    public static void error(String source, Throwable t, boolean blocking) {
        if (t == null || !isEnabled()) {
            return;
        }
        String trace = stackTrace(t);
        if (!trace.contains("dev.companion")) {
            return;
        }
        JsonObject p = new JsonObject();
        p.addProperty("source", source);
        p.addProperty("error_type", t.getClass().getName());
        p.addProperty("error_message", String.valueOf(t.getMessage()));
        p.addProperty("stack_trace", trace.length() > 8000 ? trace.substring(0, 8000) : trace);
        capture("mod_error", p, blocking);
    }

    // ---- internals ----

    private static void installCrashHandler() {
        Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, t) -> {
            try {
                error("uncaught:" + thread.getName(), t, true);
            } catch (Throwable ignored) {
            }
            if (previous != null) {
                previous.uncaughtException(thread, t);
            }
        });
    }

    private static JsonObject baseProperties() {
        FabricLoader loader = FabricLoader.getInstance();
        JsonObject p = new JsonObject();
        p.addProperty("mod_version", version("companion"));
        p.addProperty("minecraft_version", version("minecraft"));
        p.addProperty("fabric_loader_version", version("fabricloader"));
        p.addProperty("fabric_api_version", version("fabric-api"));
        p.addProperty("environment", loader.getEnvironmentType() == EnvType.CLIENT ? "client" : "server");
        p.addProperty("mods_installed", loader.getAllMods().size());
        p.addProperty("os_name", System.getProperty("os.name"));
        p.addProperty("os_arch", System.getProperty("os.arch"));
        p.addProperty("java_version", System.getProperty("java.version"));
        p.addProperty("max_memory_mb", Runtime.getRuntime().maxMemory() / (1024 * 1024));
        p.addProperty("cpu_cores", Runtime.getRuntime().availableProcessors());
        p.addProperty("locale", Locale.getDefault().toLanguageTag());
        return p;
    }

    private static String version(String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
                .map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("unknown");
    }

    private static void capture(String event, JsonObject properties, boolean blocking) {
        if (!isEnabled()) {
            return;
        }
        properties.addProperty("$geoip_disable", true);
        properties.addProperty("$lib", "kabildzon-mod");
        properties.addProperty("mod_version", version("companion"));
        JsonObject body = new JsonObject();
        body.addProperty("api_key", POSTHOG_PROJECT_KEY);
        body.addProperty("event", event);
        body.addProperty("distinct_id", config.installId);
        body.addProperty("timestamp", Instant.now().toString());
        body.add("properties", properties);

        HttpRequest request = HttpRequest.newBuilder(URI.create(POSTHOG_HOST + "/capture/"))
                .timeout(Duration.ofSeconds(blocking ? 3 : 10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();
        CompletableFuture<HttpResponse<Void>> future = HTTP.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .exceptionally(e -> {
                    LOG.debug("Telemetry send failed: {}", e.toString());
                    return null;
                });
        if (blocking) {
            try {
                future.get(3, TimeUnit.SECONDS);
            } catch (Exception ignored) {
            }
        }
    }

    private static synchronized void save() {
        try {
            Path file = FabricLoader.getInstance().getConfigDir().resolve("kabildzon-telemetry.json");
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(config));
        } catch (Exception e) {
            LOG.warn("Could not save telemetry config", e);
        }
    }

    private static String stackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
