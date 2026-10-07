package dev.companion.client;

import dev.companion.entity.CompanionEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;

/** Standard player model (wide arms). Uses the "riding" pose while sitting. */
public class CompanionModel extends PlayerEntityModel<CompanionEntity> {
    public CompanionModel(ModelPart root) {
        super(root, false);
    }

    @Override
    public void setAngles(CompanionEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.riding = entity.isInSittingPose();
        super.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
    }
}
