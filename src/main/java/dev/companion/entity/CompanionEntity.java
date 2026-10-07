package dev.companion.entity;

import dev.companion.CompanionMod;
import dev.companion.entity.ai.FetchItemGoal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.AttackWithOwnerGoal;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TrackOwnerAttackerGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.EntityView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Player-shaped companion. Tame it with an apple, then it follows you like a wolf,
 * fights with you, and fetches dropped items (apples from trees, mob drops, etc.).
 */
public class CompanionEntity extends TameableEntity {
    /** Items the companion dropped itself; it will not pick them up again. */
    private final Set<UUID> ignoredItems = new HashSet<>();

    public CompanionEntity(EntityType<? extends TameableEntity> type, World world) {
        super(type, world);
        this.setTamed(false);
        this.setEquipmentDropChance(EquipmentSlot.MAINHAND, 2.0f);
        // Always holds a gold ingot in the left (off) hand; it always drops on death.
        this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.GOLD_INGOT));
        this.setEquipmentDropChance(EquipmentSlot.OFFHAND, 2.0f);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient && this.isAlive() && !this.getOffHandStack().isOf(Items.GOLD_INGOT)) {
            this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.GOLD_INGOT));
        }
    }

    public static DefaultAttributeContainer.Builder createCompanionAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new SitGoal(this));
        this.goalSelector.add(3, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.add(4, new FetchItemGoal(this, 1.25, 10.0));
        this.goalSelector.add(5, new FollowOwnerGoal(this, 1.1, 6.0f, 2.0f, false));
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 0.9));
        this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(8, new LookAroundGoal(this));

        this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
        this.targetSelector.add(2, new AttackWithOwnerGoal(this));
        this.targetSelector.add(3, new RevengeGoal(this).setGroupRevenge());
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        World world = this.getWorld();

        if (!this.isTamed()) {
            if (stack.isOf(Items.APPLE)) {
                if (!player.getAbilities().creativeMode) {
                    stack.decrement(1);
                }
                if (!world.isClient) {
                    if (this.random.nextInt(3) == 0) {
                        this.setOwner(player);
                        this.navigation.stop();
                        this.setTarget(null);
                        this.setSitting(false);
                        world.sendEntityStatus(this, (byte) 7); // hearts
                    } else {
                        world.sendEntityStatus(this, (byte) 6); // smoke
                    }
                }
                return ActionResult.success(world.isClient);
            }
            return super.interactMob(player, hand);
        }

        if (this.isOwner(player)) {
            // Feed to heal
            if (stack.isFood() && this.getHealth() < this.getMaxHealth()) {
                if (!world.isClient) {
                    var food = stack.getItem().getFoodComponent();
                    this.heal(food != null ? food.getHunger() : 2.0f);
                    if (!player.getAbilities().creativeMode) {
                        stack.decrement(1);
                    }
                    this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 1.0f, 1.0f);
                }
                return ActionResult.success(world.isClient);
            }
            // Empty hand: sit / stand
            if (stack.isEmpty()) {
                if (!world.isClient) {
                    this.setSitting(!this.isSitting());
                    this.jumping = false;
                    this.navigation.stop();
                    this.setTarget(null);
                }
                return ActionResult.success(world.isClient);
            }
        }
        return super.interactMob(player, hand);
    }

    // ---- Fetching helpers ----

    public boolean isCarrying() {
        return !this.getMainHandStack().isEmpty();
    }

    public boolean canFetch(ItemEntity item) {
        return item.isAlive() && !item.getStack().isEmpty() && !this.ignoredItems.contains(item.getUuid());
    }

    public void pickUp(ItemEntity item) {
        ItemStack stack = item.getStack().copy();
        item.discard();
        this.equipStack(EquipmentSlot.MAINHAND, stack);
        this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 0.4f, 1.0f + (this.random.nextFloat() - 0.5f) * 0.4f);
    }

    /** Gives the carried stack to the player. Anything that does not fit is dropped at their feet. */
    public void deliverTo(PlayerEntity owner) {
        ItemStack stack = this.getMainHandStack().copy();
        this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        if (stack.isEmpty()) {
            return;
        }
        owner.getInventory().insertStack(stack);
        if (!stack.isEmpty()) {
            ItemEntity dropped = owner.dropItem(stack, false);
            if (dropped != null) {
                if (this.ignoredItems.size() > 64) {
                    this.ignoredItems.clear();
                }
                this.ignoredItems.add(dropped.getUuid());
            }
        }
        this.swingHand(Hand.MAIN_HAND);
        this.playSound(CompanionMod.COMPANION_GIVE, 1.0f, 1.0f);
    }

    @Nullable
    public PlayerEntity getOwnerPlayer() {
        LivingEntity owner = this.getOwner();
        return owner instanceof PlayerEntity player ? player : null;
    }

    // ---- Sounds ----

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return CompanionMod.COMPANION_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return CompanionMod.COMPANION_HURT;
    }

    // ---- Misc ----

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    /** Unmapped Tameable#getWorld in yarn 1.20.1. */
    @Override
    public EntityView method_48926() {
        return this.getWorld();
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }
}
