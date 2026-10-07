package dev.companion;

import dev.companion.entity.CompanionEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.world.Heightmap;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class CompanionMod implements ModInitializer {
    public static final String MOD_ID = "companion";

    public static final EntityType<CompanionEntity> COMPANION = Registry.register(
            Registries.ENTITY_TYPE,
            id("companion"),
            FabricEntityTypeBuilder.<CompanionEntity>createMob()
                    .spawnGroup(SpawnGroup.CREATURE)
                    .entityFactory(CompanionEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.8f))
                    .spawnRestriction(SpawnRestriction.Location.ON_GROUND,
                            Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn)
                    .trackRangeBlocks(10)
                    .build()
    );

    /** Spawn egg colored after the skin: dark suit + skin-tone spots. */
    public static final Item COMPANION_SPAWN_EGG = Registry.register(
            Registries.ITEM,
            id("companion_spawn_egg"),
            new SpawnEggItem(COMPANION, 0x2B2A2A, 0x87634C, new Item.Settings())
    );

    public static final SoundEvent COMPANION_HURT = registerSound("companion.hurt");
    public static final SoundEvent COMPANION_GIVE = registerSound("companion.give");
    public static final SoundEvent SWORD_DRAW = registerSound("companion.sword_draw");

    @Override
    public void onInitialize() {
        dev.companion.telemetry.Telemetry.init();
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (server.isDedicated()) {
                dev.companion.telemetry.Telemetry.sessionEnded(0, 0);
            }
        });
        FabricDefaultAttributeRegistry.register(COMPANION, CompanionEntity.createCompanionAttributes());
        SwordDrawSound.register();
        // Natural spawning: rare, single, on grass in any Overworld biome.
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.CREATURE, COMPANION, 4, 1, 1);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> entries.add(COMPANION_SPAWN_EGG));
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    private static SoundEvent registerSound(String name) {
        Identifier id = id(name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
}
