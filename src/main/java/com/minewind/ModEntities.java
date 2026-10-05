package com.minewind;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Morrowind creatures. Models are GLBs at assets/minewind/models/glb/entity/NAME.glb with the texture at
 * assets/minewind/textures/entity/NAME.png (see client/GlbEntityRenderer). Behaviour here is a first pass.
 */
public final class ModEntities {
    private static final List<Item> EGGS = new ArrayList<>();

    public static final EntityType<Mudcrab> MUDCRAB = register("mudcrab",
            EntityType.Builder.create(Mudcrab::new, SpawnGroup.CREATURE).dimensions(1.0f, 0.6f),
            Mudcrab.attributes(), 0x785537, 0xA56E46);
    public static final EntityType<Scrib> SCRIB = register("scrib",
            EntityType.Builder.create(Scrib::new, SpawnGroup.CREATURE).dimensions(0.6f, 0.5f),
            Scrib.attributes(), 0xCDBE96, 0x826E50);
    public static final EntityType<Alit> ALIT = register("alit",
            EntityType.Builder.create(Alit::new, SpawnGroup.MONSTER).dimensions(1.2f, 1.6f),
            Alit.attributes(), 0x5F7846, 0xE6C83C);
    public static final EntityType<KwamaForager> KWAMA_FORAGER = register("kwama_forager",
            EntityType.Builder.create(KwamaForager::new, SpawnGroup.MONSTER).dimensions(0.9f, 0.8f),
            KwamaForager.attributes(), 0xC8A046, 0x6E5528);

    private static <T extends MobEntity> EntityType<T> register(String id, EntityType.Builder<T> builder,
                                                               DefaultAttributeContainer.Builder attributes, int eggPrimary, int eggSecondary) {
        EntityType<T> type = Registry.register(Registries.ENTITY_TYPE, Identifier.of(MinewindMod.MOD_ID, id), builder.build(id));
        FabricDefaultAttributeRegistry.register(type, attributes);
        EGGS.add(Registry.register(Registries.ITEM, Identifier.of(MinewindMod.MOD_ID, id + "_spawn_egg"),
                new SpawnEggItem(type, eggPrimary, eggSecondary, new Item.Settings())));
        return type;
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> EGGS.forEach(entries::add));
    }

    private static DefaultAttributeContainer.Builder attrs(double health, double speed, double damage, double follow) {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, health)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, speed)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, damage)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, follow);
    }

    /** Neutral: wanders, fights back when hit. */
    public static class Mudcrab extends PathAwareEntity {
        public Mudcrab(EntityType<? extends PathAwareEntity> type, World world) { super(type, world); }
        public static DefaultAttributeContainer.Builder attributes() { return attrs(18, 0.22, 3, 16); }

        @Override
        protected void initGoals() {
            goalSelector.add(0, new SwimGoal(this));
            goalSelector.add(1, new MeleeAttackGoal(this, 1.0, false));
            goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
            goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
            goalSelector.add(7, new LookAroundGoal(this));
            targetSelector.add(1, new RevengeGoal(this));
        }
    }

    /** Passive: wanders and runs from danger. */
    public static class Scrib extends PathAwareEntity {
        public Scrib(EntityType<? extends PathAwareEntity> type, World world) { super(type, world); }
        public static DefaultAttributeContainer.Builder attributes() { return attrs(6, 0.2, 1, 12); }

        @Override
        protected void initGoals() {
            goalSelector.add(0, new SwimGoal(this));
            goalSelector.add(1, new EscapeDangerGoal(this, 1.4));
            goalSelector.add(5, new WanderAroundFarGoal(this, 0.7));
            goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
            goalSelector.add(7, new LookAroundGoal(this));
        }
    }

    /** Hostile: hunts players on sight. */
    public static class Alit extends PathAwareEntity {
        public Alit(EntityType<? extends PathAwareEntity> type, World world) { super(type, world); }
        public static DefaultAttributeContainer.Builder attributes() { return attrs(30, 0.28, 6, 24); }

        @Override
        protected void initGoals() {
            goalSelector.add(0, new SwimGoal(this));
            goalSelector.add(2, new MeleeAttackGoal(this, 1.1, false));
            goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
            goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
            goalSelector.add(7, new LookAroundGoal(this));
            targetSelector.add(1, new RevengeGoal(this));
            targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        }
    }

    /** Neutral: fights back when hit. */
    public static class KwamaForager extends PathAwareEntity {
        public KwamaForager(EntityType<? extends PathAwareEntity> type, World world) { super(type, world); }
        public static DefaultAttributeContainer.Builder attributes() { return attrs(22, 0.23, 4, 16); }

        @Override
        protected void initGoals() {
            goalSelector.add(0, new SwimGoal(this));
            goalSelector.add(1, new MeleeAttackGoal(this, 1.0, false));
            goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
            goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
            goalSelector.add(7, new LookAroundGoal(this));
            targetSelector.add(1, new RevengeGoal(this));
        }
    }

    private ModEntities() {}
}
