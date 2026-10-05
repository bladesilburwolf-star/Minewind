package com.minewind.client;

import com.minewind.MinewindMod;
import com.minewind.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

public class MinewindClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Any item model "minewind:item/NAME" that has a models/glb/NAME.glb file is replaced by the GLB;
        // items without a .glb keep their normal JSON model.
        ModelLoadingPlugin.register(plugin -> plugin.resolveModel().register(context -> {
            Identifier id = context.id();
            if (!MinewindMod.MOD_ID.equals(id.getNamespace()) || !id.getPath().startsWith("item/")) return null;
            String name = id.getPath().substring("item/".length());
            if (MinecraftClient.getInstance().getResourceManager().getResource(GlbUnbakedModel.glbPath(name)).isEmpty()) return null;
            MinewindMod.LOGGER.info("Using GLB model for {}", name);
            return new GlbUnbakedModel(name);
        }));

        // Mobs: static GLB meshes (models/glb/entity/NAME.glb, textures/entity/NAME.png)
        mob(ModEntities.MUDCRAB, "mudcrab", 0.5f);
        mob(ModEntities.SCRIB, "scrib", 0.3f);
        mob(ModEntities.ALIT, "alit", 0.7f);
        mob(ModEntities.KWAMA_FORAGER, "kwama_forager", 0.5f);
    }

    private static <T extends MobEntity> void mob(EntityType<T> type, String name, float shadow) {
        EntityRendererRegistry.register(type, context -> new GlbEntityRenderer<T>(context, name, shadow));
    }
}
