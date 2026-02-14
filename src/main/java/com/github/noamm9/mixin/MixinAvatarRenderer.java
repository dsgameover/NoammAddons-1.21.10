package com.github.noamm9.mixin;

/*
@Mixin(AvatarRenderer.class)
public class MixinAvatarRenderer {
    @Inject(method = "scale(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At("HEAD"))
    private void scale(AvatarRenderState avatarRenderState, PoseStack poseStack, CallbackInfo ci) {
        Cosmetics.preRenderCallbackScaleHook(avatarRenderState, poseStack);
    }

    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
        at = @At("HEAD")
    )
    private void extractRenderState(Avatar avatar, AvatarRenderState avatarRenderState, float f, CallbackInfo ci) {
        Cosmetics.extractRenderStateHook(avatar, avatarRenderState);
    }
}*/