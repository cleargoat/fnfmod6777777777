package com.waosfnf.client;

import com.waosfnf.FnfMod;
import com.waosfnf.game.Chart;
import com.waosfnf.game.Chart.Note;
import com.waosfnf.game.FnfConfig;
import com.waosfnf.game.FnfSongs;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Pantalla de juego estilo Friday Night Funkin'. Oponente a la izquierda, jugador a la derecha. */
public class FnfPlayScreen extends Screen {
    private static final int[] LANE_COLORS = {0xC24B99, 0x00FFFF, 0x12FA05, 0xF9393F};
    private static final double HIT_WINDOW = 166;

    private static final ResourceLocation READY = tex("ready"), SET = tex("set"), GO = tex("go");
    private static ResourceLocation tex(String n) { return new ResourceLocation(FnfMod.MODID, "textures/gui/countdown/" + n + ".png"); }

    private final Screen parent;
    private final FnfSongs.Entry entry;
    private final Chart chart;
    private final List<Note> notes;
    private FnfAudio inst, voices;

    private long t0 = 0;
    private final double leadIn, crotchet, endTime;
    private double songPos = -9999, lastPos = -9999;
    private boolean audioStarted, finished, dead;
    private int startIdx = 0;

    private final boolean[] held = new boolean[4];
    private final long[] playerGlow = new long[4], oppGlow = new long[4];

    private double health = 0.5, accSum = 0;
    private int score, combo, maxCombo, misses, judged, sick, good, bad, shit;
    private String ratingText = "";
    private int ratingColor;
    private long ratingTime;

    public FnfPlayScreen(Screen parent, FnfSongs.Entry entry, Chart chart) {
        super(Component.literal(chart.name));
        this.parent = parent;
        this.entry = entry;
        this.chart = chart;
        this.notes = chart.notes;
        this.crotchet = 60000.0 / chart.bpm;
        this.leadIn = crotchet * 4;
        this.inst = FnfAudio.load(entry.inst());
        this.voices = FnfAudio.load(entry.voices());
        Note last = notes.get(notes.size() - 1);
        double end = last.time + last.sustain + 2000;
        if (inst != null) end = Math.max(end, inst.durationMs + 300);
        this.endTime = end;
    }

    @Override
    public boolean isPauseScreen() { return true; }

    @Override
    public boolean shouldCloseOnEsc() { return false; }

    // ------------------------------------------------------------------ logica

    private void update() {
        if (finished || dead) return;
        long now = System.nanoTime();
        if (t0 == 0) t0 = now;
        songPos = (now - t0) / 1e6 - leadIn;
        double dt = lastPos < -9000 ? 0 : songPos - lastPos;
        lastPos = songPos;

        if (!audioStarted && songPos >= 0) {
            audioStarted = true;
            if (inst != null) inst.play();
            if (voices != null) voices.play();
        } else if (audioStarted && inst != null && inst.isPlaying()) {
            double ap = inst.positionMs();
            if (Math.abs(ap - songPos) > 60) { // mantener sincronizado con el audio real
                t0 = now - (long) ((ap + leadIn) * 1e6);
                songPos = ap;
            }
        }

        long nowMs = System.currentTimeMillis();
        for (int i = startIdx; i < notes.size(); i++) {
            Note n = notes.get(i);
            if (n.time - songPos > 250) break;
            if (n.done) continue;
            if (!n.player) {
                if (!n.hit && songPos >= n.time) {
                    n.hit = true;
                    n.holding = n.sustain > 0;
                    oppGlow[n.lane] = nowMs + 130;
                }
                if (n.hit) {
                    if (n.holding) {
                        oppGlow[n.lane] = nowMs + 100;
                        if (songPos >= n.time + n.sustain) { n.holding = false; n.done = true; }
                    } else n.done = true;
                }
            } else {
                if (!n.hit && !n.missed && songPos - n.time > HIT_WINDOW) {
                    n.missed = true;
                    registerMiss();
                }
                if (n.missed && songPos - (n.time + n.sustain) > 150) n.done = true;
                if (n.hit) {
                    if (n.holding) {
                        if (!held[n.lane]) { n.holding = false; n.done = true; }
                        else {
                            playerGlow[n.lane] = nowMs + 100;
                            health = Math.min(1, health + 0.00008 * dt);
                            if (songPos >= n.time + n.sustain) { n.holding = false; n.done = true; score += 100; }
                        }
                    } else n.done = true;
                }
            }
        }
        while (startIdx < notes.size() && notes.get(startIdx).done) startIdx++;

        if (health <= 0) { dead = true; stopAudio(); }
        else if (songPos > endTime) { finished = true; stopAudio(); }
    }

    private void press(int lane) {
        if (held[lane]) return;
        held[lane] = true;
        if (songPos < -HIT_WINDOW) return;
        Note best = null;
        for (int i = startIdx; i < notes.size(); i++) {
            Note n = notes.get(i);
            if (n.time - songPos > HIT_WINDOW) break;
            if (n.done || !n.player || n.hit || n.missed || n.lane != lane) continue;
            if (Math.abs(n.time - songPos) <= HIT_WINDOW) { best = n; break; }
        }
        if (best == null) return; // ghost tapping permitido
        double diff = Math.abs(best.time - songPos);
        int pts; double w, hp; String text; int color;
        if (diff <= 45) { pts = 350; w = 1.0; hp = 0.025; text = "SICK!!"; color = 0x55FFFF; sick++; }
        else if (diff <= 90) { pts = 200; w = 0.75; hp = 0.015; text = "Good"; color = 0x55FF55; good++; }
        else if (diff <= 135) { pts = 100; w = 0.5; hp = 0.005; text = "Bad"; color = 0xFFAA00; bad++; }
        else { pts = 50; w = 0.25; hp = -0.01; text = "Shit"; color = 0xFF5555; shit++; }
        best.hit = true;
        best.holding = best.sustain > 0;
        playerGlow[lane] = System.currentTimeMillis() + 150;
        combo++;
        maxCombo = Math.max(maxCombo, combo);
        score += pts;
        health = Math.min(1, health + hp);
        accSum += w;
        judged++;
        ratingText = text; ratingColor = color; ratingTime = System.currentTimeMillis();
    }

    private void registerMiss() {
        combo = 0;
        misses++;
        judged++;
        score -= 10;
        health -= 0.045;
        ratingText = "Miss"; ratingColor = 0xFF3333; ratingTime = System.currentTimeMillis();
    }

    private void stopAudio() {
        if (inst != null) { inst.stop(); inst = null; }
        if (voices != null) { voices.stop(); voices = null; }
    }

    private double accuracy() { return judged == 0 ? 100.0 : accSum / judged * 100.0; }

    // ------------------------------------------------------------------ entrada

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (finished || dead) {
            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER || key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_SPACE) onClose();
            return true;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) { onClose(); return true; }
        for (int i = 0; i < 4; i++) {
            if (key == FnfConfig.keys[i]) { press(i); return true; }
        }
        return true;
    }

    @Override
    public boolean keyReleased(int key, int scan, int mods) {
        for (int i = 0; i < 4; i++) if (key == FnfConfig.keys[i]) held[i] = false;
        return true;
    }

    @Override
    public void removed() { stopAudio(); }

    @Override
    public void onClose() {
        stopAudio();
        minecraft.setScreen(parent);
    }

    // ------------------------------------------------------------------ dibujo

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        update();
        g.fill(0, 0, width, height, 0xFF14141F);

        boolean down = FnfConfig.downscroll;
        int size = Math.min(height / 7, 56);
        int sp = (int) (size * 1.12);
        int total = sp * 3 + size;
        int playerX = (int) (width * 0.73 - total / 2.0);
        int oppX = (int) (width * 0.27 - total / 2.0);
        int strumY = down ? height - size - 20 : 28;
        double pxPerMs = 0.45 * chart.speed * (height / 720.0) * 1.6;
        long nowMs = System.currentTimeMillis();

        // receptores
        for (int lane = 0; lane < 4; lane++) {
            drawReceptor(g, oppX + lane * sp, strumY, size, lane, oppGlow[lane] > nowMs, false);
            drawReceptor(g, playerX + lane * sp, strumY, size, lane, playerGlow[lane] > nowMs, held[lane]);
        }

        // notas
        for (int i = startIdx; i < notes.size(); i++) {
            Note n = notes.get(i);
            if ((n.time - songPos) * pxPerMs > height + size) break;
            if (n.done) continue;
            int x = (n.player ? playerX : oppX) + n.lane * sp;
            boolean consumed = n.hit && !n.holding;
            if (n.sustain > 0 && !consumed) {
                double startT = (n.hit && n.holding) ? songPos : n.time;
                double yA = noteY(startT, strumY, pxPerMs, down) + size / 2.0;
                double yB = noteY(n.time + n.sustain, strumY, pxPerMs, down) + size / 2.0;
                int top = (int) Math.max(-20, Math.min(yA, yB)), bot = (int) Math.min(height + 20, Math.max(yA, yB));
                int cx = x + size / 2, hw = Math.max(2, (int) (size * 0.16));
                int alpha = n.missed ? 0x55 : 0xCC;
                if (bot > top) g.fill(cx - hw, top, cx + hw, bot, (alpha << 24) | LANE_COLORS[n.lane]);
            }
            if (!n.hit) {
                float a = n.missed ? 0.3f : 1f;
                ArrowTextures.draw(g, ArrowTextures.get("note", n.lane), x, (int) noteY(n.time, strumY, pxPerMs, down), size, a);
            }
        }

        drawHud(g, playerX, total, down);
        drawCountdown(g);
        drawResultOverlay(g);
        super.render(g, mx, my, pt);
    }

    private double noteY(double time, int strumY, double pxPerMs, boolean down) {
        double dy = (time - songPos) * pxPerMs;
        return down ? strumY - dy : strumY + dy;
    }

    private void drawReceptor(GuiGraphics g, int x, int y, int size, int lane, boolean glow, boolean pressed) {
        if (glow) {
            ArrowTextures.draw(g, ArrowTextures.get("receptor", lane), x, y, size, 0.6f);
            int gs = (int) (size * 1.4), off = (gs - size) / 2;
            ArrowTextures.draw(g, ArrowTextures.get("glow", lane), x - off, y - off, gs, 1f);
        } else if (pressed) {
            ArrowTextures.draw(g, ArrowTextures.get("pressed", lane), x, y, size, 1f);
        } else {
            ArrowTextures.draw(g, ArrowTextures.get("receptor", lane), x, y, size, 1f);
        }
    }

    private void drawHud(GuiGraphics g, int playerX, int total, boolean down) {
        int bw = Math.min(width / 2, 260), bx = (width - bw) / 2;
        int by = down ? 14 : height - 30;
        g.fill(bx - 1, by - 1, bx + bw + 1, by + 9, 0xFF000000);
        g.fill(bx, by, bx + bw, by + 8, 0xFFFF0040);
        int gw = (int) (bw * Math.max(0, Math.min(1, health)));
        g.fill(bx + bw - gw, by, bx + bw, by + 8, 0xFF4DFF4D);

        g.drawCenteredString(font, chart.name, width / 2, down ? 2 : 6, 0xFFFFFF);
        String stats = "Puntos: " + score + "  |  Fallos: " + misses + "  |  Precision: " + String.format("%.1f", accuracy()) + "%  |  Combo: " + combo;
        g.drawCenteredString(font, stats, width / 2, by + 12, 0xDDDDDD);

        long age = System.currentTimeMillis() - ratingTime;
        if (!ratingText.isEmpty() && age < 650) {
            int a = Math.max(10, (int) (255 * (1 - age / 650.0)));
            g.pose().pushPose();
            g.pose().translate(playerX + total / 2f, height / 2f, 0);
            g.pose().scale(1.6f, 1.6f, 1f);
            g.drawCenteredString(font, ratingText + (combo > 1 ? "  x" + combo : ""), 0, 0, (a << 24) | ratingColor);
            g.pose().popPose();
        }
    }

    private void drawCountdown(GuiGraphics g) {
        if (songPos >= 0 || t0 == 0) return;
        ResourceLocation t; int tw, th, target; double phaseStart;
        if (songPos >= -crotchet) { t = GO; tw = 185; th = 143; target = 110; phaseStart = -crotchet; }
        else if (songPos >= -2 * crotchet) { t = SET; tw = 185; th = 85; target = 150; phaseStart = -2 * crotchet; }
        else if (songPos >= -3 * crotchet) { t = READY; tw = 757; th = 364; target = 220; phaseStart = -3 * crotchet; }
        else return;
        double f = (songPos - phaseStart) / crotchet;
        float alpha = (float) Math.max(0.05, 1 - f * f);
        int w = (int) (target * Math.max(1f, height / 270f)), h = w * th / tw;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        g.blit(t, (width - w) / 2, (height - h) / 2, w, h, 0f, 0f, tw, th, tw, th);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private void drawResultOverlay(GuiGraphics g) {
        if (!finished && !dead) return;
        g.fill(0, 0, width, height, 0xB0000000);
        int cy = height / 2 - 50;
        g.pose().pushPose();
        g.pose().translate(width / 2f, cy, 0);
        g.pose().scale(2f, 2f, 1f);
        g.drawCenteredString(font, dead ? "GAME OVER" : "CANCION COMPLETADA", 0, 0, dead ? 0xFF5555 : 0x55FF55);
        g.pose().popPose();
        g.drawCenteredString(font, "Puntos: " + score + "   Precision: " + String.format("%.1f", accuracy()) + "%", width / 2, cy + 24, 0xFFFFFF);
        g.drawCenteredString(font, "Sick " + sick + "  Good " + good + "  Bad " + bad + "  Shit " + shit + "  Fallos " + misses, width / 2, cy + 38, 0xDDDDDD);
        g.drawCenteredString(font, "Combo maximo: " + maxCombo, width / 2, cy + 52, 0xDDDDDD);
        g.drawCenteredString(font, "Pulsa ENTER o ESC para volver", width / 2, cy + 74, 0xAAAAAA);
    }
}
