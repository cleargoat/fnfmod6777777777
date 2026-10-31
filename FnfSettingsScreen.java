package com.waosfnf.client;

import com.waosfnf.game.FnfConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class FnfSettingsScreen extends Screen {
    private static final String[] LANE_NAMES = {"Izquierda", "Abajo", "Arriba", "Derecha"};
    private final Screen parent;
    private final Button[] keyButtons = new Button[4];
    private Button scrollButton, styleButton;
    private int waiting = -1;
    private int rightX, rightY;

    public FnfSettingsScreen(Screen parent) {
        super(Component.literal("Opciones FNF"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int bw = Math.min(200, (width - 30) / 2);
        int leftX = width / 2 - bw - 5;
        rightX = width / 2 + 5;
        int y = 40;
        rightY = y;
        for (int i = 0; i < 4; i++) {
            final int lane = i;
            keyButtons[i] = addRenderableWidget(Button.builder(Component.empty(), b -> { waiting = lane; refresh(); })
                    .bounds(leftX, y + i * 24, bw, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Restablecer flechitas"), b -> {
            FnfConfig.resetKeys(); FnfConfig.save(); waiting = -1; refresh();
        }).bounds(leftX, y + 4 * 24, bw, 20).build());

        scrollButton = addRenderableWidget(Button.builder(Component.empty(), b -> {
            FnfConfig.downscroll = !FnfConfig.downscroll; FnfConfig.save(); refresh();
        }).bounds(rightX, y, bw, 20).build());
        styleButton = addRenderableWidget(Button.builder(Component.empty(), b -> {
            FnfConfig.pixelArrows = !FnfConfig.pixelArrows; FnfConfig.save(); refresh();
        }).bounds(rightX, y + 24, bw, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Listo"), b -> onClose())
                .bounds(width / 2 - 50, height - 30, 100, 20).build());
        refresh();
    }

    private void refresh() {
        for (int i = 0; i < 4; i++) {
            String key = waiting == i ? "> presiona una tecla <"
                    : InputConstants.Type.KEYSYM.getOrCreate(FnfConfig.keys[i]).getDisplayName().getString();
            keyButtons[i].setMessage(Component.literal("Flecha " + LANE_NAMES[i] + ": " + key));
        }
        scrollButton.setMessage(Component.literal("Scroll: " + (FnfConfig.downscroll ? "Downscroll (flechas abajo)" : "Upscroll (flechas arriba)")));
        styleButton.setMessage(Component.literal("Flechas: " + (FnfConfig.pixelArrows ? "Pixeladas" : "Originales de FNF")));
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (waiting >= 0) {
            if (key != GLFW.GLFW_KEY_ESCAPE) {
                FnfConfig.keys[waiting] = key;
                FnfConfig.save();
            }
            waiting = -1;
            refresh();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        g.drawCenteredString(font, "Opciones FNF", width / 2, 16, 0xFFFFFF);
        int size = 36, sp = 40, x0 = rightX, y0 = rightY + 56;
        g.drawString(font, "Vista previa:", x0, y0 - 11, 0xAAAAAA);
        for (int i = 0; i < 4; i++) {
            ArrowTextures.draw(g, ArrowTextures.get("receptor", i), x0 + i * sp, y0, size, 1f);
            ArrowTextures.draw(g, ArrowTextures.get("note", i), x0 + i * sp, y0 + size + 4, size, 1f);
        }
        super.render(g, mx, my, pt);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
