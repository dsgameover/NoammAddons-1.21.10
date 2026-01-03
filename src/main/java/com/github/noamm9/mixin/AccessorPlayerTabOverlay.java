package com.github.noamm9.mixin;

import com.github.noamm9.interfaces.ITabList;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerTabOverlay.class)
public interface AccessorPlayerTabOverlay extends ITabList {
    @Accessor("header")
    Component getHeader();

    @Accessor("footer")
    Component getFooter();
}
