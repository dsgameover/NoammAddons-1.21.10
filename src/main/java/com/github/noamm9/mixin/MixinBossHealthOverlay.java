package com.github.noamm9.mixin;

import com.github.noamm9.features.impl.dungeon.BossBarHealth;
import com.github.noamm9.utils.location.LocationUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// todo config check?
@Mixin(BossHealthOverlay.class)
public class MixinBossHealthOverlay {
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/LerpingBossEvent;getName()Lnet/minecraft/network/chat/Component;"))
    public Component onRender(LerpingBossEvent instance, Operation<Component> original) {
        Component originalName = original.call(instance);
        if (!BossBarHealth.INSTANCE.enabled) return originalName;
        if (!LocationUtils.inDungeon) return originalName;

        float maxHealth = BossBarHealth.getMaxHealth(originalName);
        if (maxHealth == -1F) return originalName;

        float currentHealth = ((ILerpingBossEvent) instance).getTargetPrecent() * maxHealth;
        return originalName.copy().append(
            Component.literal(" §r§8- §a" + BossBarHealth.formatHealth(currentHealth) + "§7/§a" + BossBarHealth.formatHealth(maxHealth) + "§c❤")
        );
    }
}