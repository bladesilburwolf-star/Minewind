package com.minewind.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

/** A static, pre-baked list of quads (from a .glb) shown as an item model. */
public final class GlbBakedModel implements BakedModel {
    // Same display transforms as vanilla item/handheld.json (translations are in 1/16 block units there).
    private static final ModelTransformation HANDHELD = new ModelTransformation(
            t(0, 90, -55, 0, 4.0f, 0.5f, 0.85f),      // third person, left hand
            t(0, -90, 55, 0, 4.0f, 0.5f, 0.85f),      // third person, right hand
            t(0, 90, -25, 1.13f, 3.2f, 1.13f, 0.68f), // first person, left hand
            t(0, -90, 25, 1.13f, 3.2f, 1.13f, 0.68f), // first person, right hand
            t(0, 180, 0, 0, 13, 7, 1),                // head
            t(0, 0, 0, 0, 0, 0, 1),                   // gui
            t(0, 0, 0, 0, 2, 0, 0.5f),                // ground
            t(0, 180, 0, 0, 0, 0, 1));                // fixed (item frame)

    private static Transformation t(float rx, float ry, float rz, float tx, float ty, float tz, float s) {
        return new Transformation(new Vector3f(rx, ry, rz), new Vector3f(tx / 16f, ty / 16f, tz / 16f), new Vector3f(s, s, s));
    }

    private final List<BakedQuad> quads;
    private final Sprite particle;

    public GlbBakedModel(List<BakedQuad> quads, Sprite particle) {
        this.quads = quads;
        this.particle = particle;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        // Everything is emitted as "no cull face" quads.
        return face == null ? quads : List.of();
    }

    @Override public boolean useAmbientOcclusion() { return false; }
    @Override public boolean hasDepth() { return true; }
    @Override public boolean isSideLit() { return true; }
    @Override public boolean isBuiltin() { return false; }
    @Override public Sprite getParticleSprite() { return particle; }
    @Override public ModelTransformation getTransformation() { return HANDHELD; }
    @Override public ModelOverrideList getOverrides() { return ModelOverrideList.EMPTY; }
}
