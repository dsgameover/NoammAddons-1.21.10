package com.github.noamm9.mixin;

import com.github.noamm9.features.impl.dungeon.TeammateESP;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer extends Player implements ClientAvatarEntity {
    public MixinAbstractClientPlayer(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Inject(method = "belowNameDisplay", at = @At("HEAD"), cancellable = true)
    private void cancelNametag(CallbackInfoReturnable<Component> cir) {
        if (TeammateESP.shouldHideNametag(this)) {
            cir.setReturnValue(null);
        }
    }
}