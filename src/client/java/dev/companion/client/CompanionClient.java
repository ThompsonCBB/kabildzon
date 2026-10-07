package dev.companion.client;

import dev.companion.CompanionMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;

public class CompanionClient implements ClientModInitializer {
    public static final EntityModelLayer COMPANION_LAYER = new EntityModelLayer(CompanionMod.id("companion"), "main");

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(COMPANION_LAYER,
                () -> TexturedModelData.of(PlayerEntityModel.getTexturedModelData(Dilation.NONE, false), 64, 64));
        EntityRendererRegistry.register(CompanionMod.COMPANION, CompanionRenderer::new);
        TelemetryClient.register();
    }
}
