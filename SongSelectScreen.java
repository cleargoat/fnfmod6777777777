package com.waosfnf.client;

import com.waosfnf.game.Chart;
import com.waosfnf.game.FnfSongs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SongSelectScreen extends Screen {
    private static final int PER_PAGE = 6;
    private final Screen parent;
    private List<FnfSongs.Entry> songs;
    private int page = 0;
    private String error = "";

    public SongSelectScreen(Screen parent) {
        super(Component.literal("Canciones"));
        this.parent = parent;
        FnfSongs.ensureFolders();
        this.songs = FnfSongs.scan();
    }

    @Override
    protected void init() {
        int cx = width / 2, y = 40;
        int pages = Math.max(1, (songs.size() + PER_PAGE - 1) / PER_PAGE);
        page = Math.min(page, pages - 1);
        for (int i = page * PER_PAGE; i < Math.min(songs.size(), (page + 1) * PER_PAGE); i++) {
            FnfSongs.Entry e = songs.get(i);
            String label = e.label() + (e.inst() == null ? "  (sin audio)" : "");
            addRenderableWidget(Button.builder(Component.literal(label), b -> start(e))
                    .bounds(cx - 120, y, 240, 20).build());
            y += 24;
        }
        int by = height - 52;
        Button prev = Button.builder(Component.literal("<"), b -> { page--; rebuildWidgets(); }).bounds(cx - 120, by, 40, 20).build();
        Button next = Button.builder(Component.literal(">"), b -> { page++; rebuildWidgets(); }).bounds(cx + 80, by, 40, 20).build();
        prev.active = page > 0;
        next.active = page < pages - 1;
        addRenderableWidget(prev);
        addRenderableWidget(next);
        addRenderableWidget(Button.builder(Component.literal("Volver"), b -> onClose()).bounds(cx - 50, by, 100, 20).build());
    }

    private void start(FnfSongs.Entry e) {
        try {
            Chart chart = Chart.load(e.chart());
            minecraft.getMusicManager().stopPlaying();
            minecraft.setScreen(new FnfPlayScreen(this, e, chart));
        } catch (Exception ex) {
            error = "Error al leer el chart: " + ex.getMessage();
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        g.drawCenteredString(font, "Elige una cancion", width / 2, 16, 0xFFFFFF);
        if (songs.isEmpty()) {
            g.drawCenteredString(font, "No hay canciones en fnf/songs", width / 2, height / 2 - 10, 0xFFAA00);
        }
        if (!error.isEmpty()) g.drawCenteredString(font, error, width / 2, height - 70, 0xFF5555);
        super.render(g, mx, my, pt);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
