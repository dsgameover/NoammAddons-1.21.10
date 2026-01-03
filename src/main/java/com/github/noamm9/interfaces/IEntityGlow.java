package com.github.noamm9.interfaces;

public interface IEntityGlow {
    int getCustomGlowColor();
    void setCustomGlowColor(int color);

    boolean isGlowForced();
    void setGlowForced(boolean forced);
}
