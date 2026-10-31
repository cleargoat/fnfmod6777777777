package com.waosfnf.client;

import com.waosfnf.FnfMod;
import com.waosfnf.game.FnfConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/** Texturas de flechas (todas 128x128). kind = receptor | note | pressed | glow. Carril 0=izq 1=abajo 2=arriba 3=der. */
public final class ArrowTextures {
    private static final String[] DIR = {"left", "down", "up", "right"};
    private static final Map<String, ResourceLocation> CACHE = new HashMap<>();
    public static final int TEX = 128;

    private ArrowTextures() {}

    public static ResourceLocation get(String kind, int lane) {
        return get(FnfConfig.pixelArrows, kind, lane);
    }

    public static ResourceLocation get(boolean pixel, String kind, int lane) {
        String path = "textures/gui/arrows/" + (pixel ? "pixel" : "original") + "/" + kind + "_" + DIR[lane] + ".png";
        return CACHE.computeIfAbsent(path, p -> new ResourceLocation(FnfMod.MODID, p));
    }

    public static void draw(GuiGraphics g, ResourceLocation tex, int x, int y, int size, float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        g.blit(tex, x, y, size, size, 0f, 0f, TEX, TEX, TEX, TEX);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
}
