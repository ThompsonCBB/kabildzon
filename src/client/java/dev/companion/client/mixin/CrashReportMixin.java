package dev.companion.client.mixin;

import dev.companion.telemetry.Telemetry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Reports game crashes caused by this mod before the game exits. */
@Mixin(MinecraftClient.class)
public class CrashReportMixin {
    @Inject(method = "printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V", at = @At("HEAD"))
    private static void kabildzon$onCrash(CrashReport report, CallbackInfo ci) {
        try {
            Telemetry.error("crash", report.getCause(), true);
        } catch (Throwable ignored) {
        }
    }
}
