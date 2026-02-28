package com.github.noamm9.mixin;

import com.github.noamm9.features.impl.visual.RevertAxes;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Objects;

@Mixin(ItemModelResolver.class)
public class MixinLivingEntityItem {
    @ModifyVariable(
            method = "updateForLiving",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private ItemStack revertAxe(ItemStack original) {
        if (original == null || original.isEmpty()) {
            return original;
        }

        ItemStack replacement = RevertAxes.INSTANCE.shouldReplace(original);
        return Objects.requireNonNullElse(replacement, original);
    }
}
