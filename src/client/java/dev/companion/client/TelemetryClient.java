package dev.companion.client;

import dev.companion.telemetry.Telemetry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Client-side session tracking for telemetry. */
public final class TelemetryClient {
    private static long joinedAt = -1;
    private static long playMillis = 0;
    private static int worlds = 0;

    private TelemetryClient() {
    }

    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            joinedAt = System.currentTimeMillis();
            worlds++;
            Telemetry.worldJoined(client.isInSingleplayer());
            if (Telemetry.isEnabled() && Telemetry.consumeNotice() && client.player != null) {
                client.player.sendMessage(Text.literal("[Kabildzon] ").formatted(Formatting.GOLD)
                        .append(Text.literal("Anonymous usage stats are enabled (versions, OS, session length, mod errors). "
                                + "Turn off in config/kabildzon-telemetry.json").formatted(Formatting.GRAY)), false);
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (joinedAt > 0) {
                playMillis += System.currentTimeMillis() - joinedAt;
                joinedAt = -1;
            }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            if (joinedAt > 0) {
                playMillis += System.currentTimeMillis() - joinedAt;
                joinedAt = -1;
            }
            Telemetry.sessionEnded(playMillis / 1000, worlds);
        });
    }
}
