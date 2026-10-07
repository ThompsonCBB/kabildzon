package dev.companion.client;

import dev.companion.CompanionMod;
import dev.companion.entity.CompanionEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class CompanionRenderer extends BipedEntityRenderer<CompanionEntity, CompanionModel> {
    private static final Identifier TEXTURE = CompanionMod.id("textures/entity/companion.png");

    public CompanionRenderer(EntityRendererFactory.Context context) {
        super(context, new CompanionModel(context.getPart(CompanionClient.COMPANION_LAYER)), 0.5f);
    }

    @Override
    public void render(CompanionEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        if (entity.isInSittingPose()) {
            matrices.translate(0.0, -0.6, 0.0);
        }
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        matrices.pop();
    }

    @Override
    public Identifier getTexture(CompanionEntity entity) {
        return TEXTURE;
    }
}
