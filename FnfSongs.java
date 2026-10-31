package com.waosfnf.game;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Carpeta "fnf" del modpack: fnf/songs/<cancion>/chart.json + Inst.ogg + Voices.ogg */
public final class FnfSongs {
    public record Entry(String label, Path chart, Path inst, Path voices) {}

    private FnfSongs() {}

    public static Path root() { return FMLPaths.GAMEDIR.get().resolve("fnf"); }
    public static Path songs() { return root().resolve("songs"); }

    private static final String README = """
        WaosFNF - carpeta de canciones
        ================================
        Cada cancion va en su propia carpeta dentro de fnf/songs/:

          fnf/songs/bopeebo/chart.json      (chart en formato FNF vanilla / Psych Engine)
          fnf/songs/bopeebo/Inst.ogg        (instrumental)
          fnf/songs/bopeebo/Voices.ogg      (voces, opcional)

        - Puedes poner varios .json en la misma carpeta (ej. bopeebo-easy.json, bopeebo-hard.json);
          cada uno sale como una entrada distinta en el menu.
        - El audio debe estar en formato .ogg (Vorbis).
        - Si no hay Inst.ogg la cancion se juega en silencio.
        - "tutorial" viene con un chart de ejemplo; agrega tu Inst.ogg para escucharlo.
        - Para canciones de otros mods de FNF, copia su chart (.json) y sus .ogg en una carpeta nueva.
        - Dentro del juego pulsa U para abrir el menu.
        """;

    public static void ensureFolders() {
        try {
            Path tut = songs().resolve("tutorial");
            Files.createDirectories(tut);
            Path chart = tut.resolve("chart.json");
            if (!Files.exists(chart)) {
                try (InputStream in = FnfSongs.class.getResourceAsStream("/assets/waosfnf/default_songs/tutorial/chart.json")) {
                    if (in != null) Files.copy(in, chart);
                }
            }
            Path readme = root().resolve("LEEME.txt");
            if (!Files.exists(readme)) Files.writeString(readme, README);
        } catch (IOException e) {
            LogUtils.getLogger().warn("[WaosFNF] No se pudo crear la carpeta fnf", e);
        }
    }

    public static List<Entry> scan() {
        List<Entry> out = new ArrayList<>();
        try (DirectoryStream<Path> dirs = Files.newDirectoryStream(songs(), Files::isDirectory)) {
            for (Path dir : dirs) {
                Path inst = findFile(dir, "inst.ogg");
                Path voices = findFile(dir, "voices.ogg");
                try (DirectoryStream<Path> jsons = Files.newDirectoryStream(dir, "*.json")) {
                    for (Path j : jsons) {
                        String stem = j.getFileName().toString();
                        stem = stem.substring(0, stem.length() - 5);
                        String label = dir.getFileName().toString();
                        if (!stem.equalsIgnoreCase("chart") && !stem.equalsIgnoreCase(label)) label += " (" + stem + ")";
                        out.add(new Entry(label, j, inst, voices));
                    }
                }
            }
        } catch (IOException ignored) {}
        out.sort(Comparator.comparing(Entry::label, String.CASE_INSENSITIVE_ORDER));
        return out;
    }

    /** Busca por nombre de carpeta o por etiqueta (sin distinguir mayusculas). */
    public static Entry find(String name) {
        List<Entry> all = scan();
        for (Entry e : all) if (e.label().equalsIgnoreCase(name)) return e;
        for (Entry e : all) if (e.chart().getParent().getFileName().toString().equalsIgnoreCase(name)) return e;
        return null;
    }

    private static Path findFile(Path dir, String lowerName) throws IOException {
        try (DirectoryStream<Path> s = Files.newDirectoryStream(dir)) {
            for (Path p : s) if (p.getFileName().toString().toLowerCase(Locale.ROOT).equals(lowerName)) return p;
        }
        return null;
    }
}
