package com.waosfnf.client;

import com.waosfnf.game.FnfSongs;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Menu principal (tecla U). */
public class FnfMenuScreen extends Screen {
    public FnfMenuScreen() {
        super(Component.literal("WaosFNF"));
    }

    @Override
    protected void init() {
        int cx = width / 2, y = height / 2 - 30;
        addRenderableWidget(Button.builder(Component.literal("Jugar"), b -> minecraft.setScreen(new SongSelectScreen(this)))
                .bounds(cx - 100, y, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Opciones (teclas / downscroll / flechas)"), b -> minecraft.setScreen(new FnfSettingsScreen(this)))
                .bounds(cx - 100, y + 24, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Abrir carpeta de canciones"), b -> {
            FnfSongs.ensureFolders();
            Util.getPlatform().openFile(FnfSongs.songs().toFile());
        }).bounds(cx - 100, y + 48, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cerrar"), b -> onClose())
                .bounds(cx - 100, y + 80, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int size = 32, sp = 36, x0 = width / 2 - (sp * 3 + size) / 2, y0 = height / 2 - 100;
        for (int i = 0; i < 4; i++) ArrowTextures.draw(g, ArrowTextures.get("note", i), x0 + i * sp, y0, size, 1f);
        g.pose().pushPose();
        g.pose().translate(width / 2f, y0 + size + 8, 0);
        g.pose().scale(2f, 2f, 1f);
        g.drawCenteredString(font, "WaosFNF", 0, 0, 0xFFFFFF);
        g.pose().popPose();
        super.render(g, mx, my, pt);
    }
}
