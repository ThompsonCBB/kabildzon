package dev.companion.entity.ai;

import dev.companion.entity.CompanionEntity;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Finds dropped items near the owner, runs to them, picks them up and brings them back.
 */
public class FetchItemGoal extends Goal {
    private static final int MAX_TICKS = 20 * 20; // give up on one item after 20s

    private final CompanionEntity companion;
    private final double speed;
    private final double searchRadius;

    @Nullable
    private ItemEntity target;
    private int ticks;
    private int repathCooldown;

    public FetchItemGoal(CompanionEntity companion, double speed, double searchRadius) {
        this.companion = companion;
        this.speed = speed;
        this.searchRadius = searchRadius;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (!this.companion.isTamed() || this.companion.isSitting() || this.companion.isLeashed()) {
            return false;
        }
        PlayerEntity owner = this.companion.getOwnerPlayer();
        if (owner == null || owner.isSpectator() || this.companion.getTarget() != null) {
            return false;
        }
        if (this.companion.isCarrying()) {
            return true;
        }
        this.target = this.findItem(owner);
        return this.target != null;
    }

    @Override
    public boolean shouldContinue() {
        if (!this.companion.isTamed() || this.companion.isSitting() || this.ticks > MAX_TICKS) {
            return false;
        }
        PlayerEntity owner = this.companion.getOwnerPlayer();
        if (owner == null || !owner.isAlive()) {
            return false;
        }
        if (this.companion.isCarrying()) {
            return true;
        }
        return this.target != null && this.companion.canFetch(this.target);
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.repathCooldown = 0;
    }

    @Override
    public void stop() {
        this.target = null;
        this.companion.getNavigation().stop();
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.ticks++;
        PlayerEntity owner = this.companion.getOwnerPlayer();
        if (owner == null) {
            return;
        }

        if (this.companion.isCarrying()) {
            // Bring the item back
            this.companion.getLookControl().lookAt(owner, 30.0f, 30.0f);
            if (this.companion.squaredDistanceTo(owner) < 2.5 * 2.5) {
                this.companion.getNavigation().stop();
                this.companion.deliverTo(owner);
                this.ticks = 0;
                this.target = this.findItem(owner); // chain to the next item, if any
            } else {
                this.moveTo(owner.getX(), owner.getY(), owner.getZ(), owner);
            }
            return;
        }

        if (this.target == null) {
            return;
        }
        this.companion.getLookControl().lookAt(this.target, 30.0f, 30.0f);
        if (this.companion.squaredDistanceTo(this.target) < 1.5 * 1.5) {
            this.companion.getNavigation().stop();
            this.companion.pickUp(this.target);
            this.target = null;
            this.ticks = 0;
        } else {
            this.moveTo(this.target.getX(), this.target.getY(), this.target.getZ(), null);
        }
    }

    private void moveTo(double x, double y, double z, @Nullable PlayerEntity owner) {
        if (--this.repathCooldown > 0) {
            return;
        }
        this.repathCooldown = 10;
        if (owner != null) {
            this.companion.getNavigation().startMovingTo(owner, this.speed);
        } else {
            this.companion.getNavigation().startMovingTo(x, y, z, this.speed);
        }
    }

    @Nullable
    private ItemEntity findItem(PlayerEntity owner) {
        List<ItemEntity> items = this.companion.getWorld().getEntitiesByClass(
                ItemEntity.class,
                this.companion.getBoundingBox().expand(this.searchRadius, 4.0, this.searchRadius),
                item -> this.companion.canFetch(item)
                        && item.squaredDistanceTo(owner) > 2.0 * 2.0
                        && item.squaredDistanceTo(owner) < 16.0 * 16.0
                        && !item.isInLava()
        );
        return items.stream()
                .min(Comparator.comparingDouble(this.companion::squaredDistanceTo))
                .orElse(null);
    }
}
