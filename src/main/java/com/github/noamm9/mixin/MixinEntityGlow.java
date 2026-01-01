package com.github.noamm9.mixin;

import com.github.noamm9.features.impl.render.StarMobEsp;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinEntityGlow {
    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    public void onIsCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
        var entity = (Entity)(Object) this;
        if (StarMobEsp.getStarMobs().contains(entity)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    public void onGetTeamColorValue(CallbackInfoReturnable<Integer> cir) {
        var entity = (Entity)(Object) this;
        if (StarMobEsp.getStarMobs().contains(entity)) {
            cir.setReturnValue(0xFFFF00);
        }
    }
}