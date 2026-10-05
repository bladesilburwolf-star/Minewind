package com.minewind.client;

import com.minewind.MinewindMod;
import com.minewind.client.glb.GlbLoader;
import com.minewind.client.glb.GlbMesh;
import com.minewind.client.glb.GlbOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Loads assets/minewind/models/glb/NAME.glb (+ optional NAME.json options) and bakes it into quads.
 * The texture is taken from assets/minewind/textures/item/NAME.png, which the glb's UVs map onto.
 */
public final class GlbUnbakedModel implements UnbakedModel {
    private final String name;

    public GlbUnbakedModel(String name) {
        this.name = name;
    }

    public static Identifier glbPath(String name) {
        return Identifier.of(MinewindMod.MOD_ID, "models/glb/" + name + ".glb");
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return List.of();
    }

    @Override
    public void setParents(Function<Identifier, UnbakedModel> modelLoader) {
    }

    @Nullable
    @Override
    public BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer) {
        SpriteIdentifier spriteId = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier.of(MinewindMod.MOD_ID, "item/" + name));
        Sprite sprite = textureGetter.apply(spriteId);
        try {
            ResourceManager resources = MinecraftClient.getInstance().getResourceManager();
            Optional<Resource> glb = resources.getResource(glbPath(name));
            if (glb.isEmpty()) return null;

            GlbOptions options = GlbOptions.defaults();
            Optional<Resource> opt = resources.getResource(Identifier.of(MinewindMod.MOD_ID, "models/glb/" + name + ".json"));
            if (opt.isPresent()) {
                try (InputStream in = opt.get().getInputStream()) {
                    options = GlbOptions.fromJson(new String(in.readAllBytes(), StandardCharsets.UTF_8));
                }
            }
            byte[] bytes;
            try (InputStream in = glb.get().getInputStream()) {
                bytes = in.readAllBytes();
            }
            GlbMesh mesh = GlbLoader.load(bytes, options);
            return new GlbBakedModel(buildQuads(mesh, sprite), sprite);
        } catch (IOException | RuntimeException e) {
            MinewindMod.LOGGER.error("Failed to load GLB model '{}'", name, e);
            return null; // falls back to the missing model instead of crashing the game
        }
    }

    private static List<BakedQuad> buildQuads(GlbMesh mesh, Sprite sprite) {
        float u0 = sprite.getMinU(), u1 = sprite.getMaxU(), v0 = sprite.getMinV(), v1 = sprite.getMaxV();
        List<BakedQuad> quads = new ArrayList<>(mesh.triangleCount());
        for (int t = 0; t < mesh.triangleCount(); t++) {
            int[] data = new int[32]; // 4 vertices x 8 ints: x y z color u v light normal
            for (int k = 0; k < 4; k++) {
                int vi = t * 3 + Math.min(k, 2); // triangle -> quad by repeating the last vertex
                int o = k * 8;
                data[o] = Float.floatToRawIntBits(mesh.pos[vi * 3]);
                data[o + 1] = Float.floatToRawIntBits(mesh.pos[vi * 3 + 1]);
                data[o + 2] = Float.floatToRawIntBits(mesh.pos[vi * 3 + 2]);
                data[o + 3] = -1;
                float u = clamp01(mesh.uv[vi * 2]), v = clamp01(mesh.uv[vi * 2 + 1]);
                data[o + 4] = Float.floatToRawIntBits(u0 + u * (u1 - u0));
                data[o + 5] = Float.floatToRawIntBits(v0 + v * (v1 - v0));
                data[o + 6] = 0;
                data[o + 7] = packNormal(mesh.nrm[vi * 3], mesh.nrm[vi * 3 + 1], mesh.nrm[vi * 3 + 2]);
            }
            int n0 = t * 3;
            Direction face = Direction.getFacing(mesh.nrm[n0 * 3], mesh.nrm[n0 * 3 + 1], mesh.nrm[n0 * 3 + 2]);
            quads.add(new BakedQuad(data, -1, face, sprite, true));
        }
        return quads;
    }

    private static float clamp01(float f) {
        return f < 0 ? 0 : Math.min(f, 1);
    }

    private static int packNormal(float x, float y, float z) {
        return ((int) (x * 127) & 0xFF) | (((int) (y * 127) & 0xFF) << 8) | (((int) (z * 127) & 0xFF) << 16);
    }
}
