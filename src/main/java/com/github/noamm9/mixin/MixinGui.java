package com.github.noamm9.mixin;

import com.github.noamm9.event.EventBus;
import com.github.noamm9.event.impl.RenderOverlayEvent;
import com.github.noamm9.ui.ClientBranding;
import com.github.noamm9.utils.dungeons.DungeonDebugHUD;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MixinGui {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private PlayerTabOverlay tabList;

    @Inject(method = "render", at = @At("TAIL"))
    public void onRenderHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (this.minecraft.options.hideGui) return;
        EventBus.post(new RenderOverlayEvent(guiGraphics, deltaTracker));
        DungeonDebugHUD.render(guiGraphics);
    }

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    public void onRenderPortalOverlay(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "renderConfusionOverlay", at = @At("HEAD"), cancellable = true)
    public void onRenderConfusionOverlay(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
        ci.cancel();
    }


    @Inject(method = "displayScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    public void a(GuiGraphics guiGraphics, Objective objective, CallbackInfo ci) {
        ci.cancel();

        ClientBranding.drawScoreboard(guiGraphics, objective);
    }


    @Inject(method = "renderTabList", at = @At("HEAD"), cancellable = true)
    public void aa(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ci.cancel();

        Scoreboard scoreboard = this.minecraft.level.getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(DisplaySlot.LIST);
        if (!this.minecraft.options.keyPlayerList.isDown()
            || this.minecraft.isLocalServer() && this.minecraft.player.connection.getListedOnlinePlayers().size() <= 1 && objective == null) {
            this.tabList.setVisible(false);
        } else {
            this.tabList.setVisible(true);
            guiGraphics.nextStratum();
            ClientBranding.drawTablist(guiGraphics);
        }
    }
}
