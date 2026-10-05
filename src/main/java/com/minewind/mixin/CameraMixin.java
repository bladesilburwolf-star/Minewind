package com.minewind.mixin;

import com.minewind.MinewindMod;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void minewind$beforeTick(CallbackInfo ci) {
        if (MinewindMod.getInstance() != null) {
            ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
            MinewindMod.getInstance().getMorrowindSystems().getMovementSystem().updateMovementState(player);
        }
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void minewind$travel(Vec3d movementInput, CallbackInfo ci) {
        if (MinewindMod.getInstance() != null) {
            ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
            Vec3d custom = MinewindMod.getInstance().getMorrowindSystems().getMovementSystem().calculateMovementVelocity(player, movementInput);
            player.setVelocity(custom);
            ci.cancel();
        }
    }
}
