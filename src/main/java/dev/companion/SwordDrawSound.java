package dev.companion;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.SwordItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;

/** Plays a sound (heard by everyone nearby) when a player switches to a sword in the main hand. */
public final class SwordDrawSound {
    private static final Map<UUID, Boolean> HOLDING_SWORD = new HashMap<>();

    private SwordDrawSound() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                boolean holding = player.getMainHandStack().getItem() instanceof SwordItem;
                Boolean before = HOLDING_SWORD.put(player.getUuid(), holding);
                if (holding && before != null && !before && !player.isSpectator()) {
                    player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                            CompanionMod.SWORD_DRAW, SoundCategory.PLAYERS, 1.0f, 1.0f);
                }
            }
        });
    }
}
