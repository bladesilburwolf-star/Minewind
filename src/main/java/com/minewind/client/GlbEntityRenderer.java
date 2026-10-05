package com.minewind.client;

import com.minewind.MinewindMod;
import com.minewind.client.glb.GlbLoader;
import com.minewind.client.glb.GlbMesh;
import com.minewind.client.glb.GlbOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Draws a mob as a static GLB mesh: models/glb/entity/NAME.glb (+ optional NAME.json) with the texture
 * textures/entity/NAME.png. The model should face +Z (glTF "front"); use "rotate":[0,180,0] if it faces the wrong way.
 * No animation yet - the mesh just moves and turns with the entity.
 */
public final class GlbEntityRenderer<T extends MobEntity> extends EntityRenderer<T> {
    private final String name;
    private final Identifier texture;
    private GlbMesh mesh;
    private boolean loadFailed;

    public GlbEntityRenderer(EntityRendererFactory.Context context, String name, float shadowRadius) {
        super(context);
        this.name = name;
        this.texture = Identifier.of(MinewindMod.MOD_ID, "textures/entity/" + name + ".png");
        this.shadowRadius = shadowRadius;
    }

    @Override
    public Identifier getTexture(T entity) {
        return texture;
    }

    private void load() {
        try {
            ResourceManager resources = MinecraftClient.getInstance().getResourceManager();
            Optional<Resource> glb = resources.getResource(Identifier.of(MinewindMod.MOD_ID, "models/glb/entity/" + name + ".glb"));
            if (glb.isEmpty()) throw new IOException("missing models/glb/entity/" + name + ".glb");
            GlbOptions options = GlbOptions.entityDefaults();
            Optional<Resource> opt = resources.getResource(Identifier.of(MinewindMod.MOD_ID, "models/glb/entity/" + name + ".json"));
            if (opt.isPresent()) {
                try (InputStream in = opt.get().getInputStream()) {
                    options = GlbOptions.fromJson(new String(in.readAllBytes(), StandardCharsets.UTF_8), true);
                }
            }
            try (InputStream in = glb.get().getInputStream()) {
                mesh = GlbLoader.load(in.readAllBytes(), options);
            }
        } catch (IOException | RuntimeException e) {
            loadFailed = true;
            MinewindMod.LOGGER.error("Failed to load GLB entity model '{}'", name, e);
        }
    }

    @Override
    public void render(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (mesh == null && !loadFailed) load();
        if (mesh != null) {
            matrices.push();
            float body = MathHelper.lerpAngleDegrees(tickDelta, entity.prevBodyYaw, entity.bodyYaw);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-body));
            if (entity.deathTime > 0) {
                float fall = MathHelper.sqrt(Math.min((entity.deathTime + tickDelta - 1.0f) / 20.0f * 1.6f, 1.0f));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(fall * 90.0f));
            }
            VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(texture));
            MatrixStack.Entry entry = matrices.peek();
            int overlay = LivingEntityRenderer.getOverlay(entity, 0.0f);
            for (int t = 0; t < mesh.triangleCount(); t++) {
                for (int k = 0; k < 4; k++) { // triangle -> quad by repeating the last vertex
                    int v = t * 3 + Math.min(k, 2);
                    vc.vertex(entry, mesh.pos[v * 3], mesh.pos[v * 3 + 1], mesh.pos[v * 3 + 2])
                            .color(255, 255, 255, 255)
                            .texture(mesh.uv[v * 2], mesh.uv[v * 2 + 1])
                            .overlay(overlay)
                            .light(light)
                            .normal(entry, mesh.nrm[v * 3], mesh.nrm[v * 3 + 1], mesh.nrm[v * 3 + 2]);
                }
            }
            matrices.pop();
        }
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light); // name tag
    }
}
