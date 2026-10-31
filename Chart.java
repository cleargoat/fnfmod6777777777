package com.waosfnf.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Lee charts de FNF (vanilla y Psych Engine). Los tiempos del chart ya vienen en milisegundos. */
public class Chart {
    public static class Note {
        public final double time;
        public final int lane;
        public final double sustain;
        public final boolean player;
        public boolean hit, missed, holding, done;

        Note(double time, int lane, double sustain, boolean player) {
            this.time = time; this.lane = lane; this.sustain = sustain; this.player = player;
        }
    }

    public String name = "Cancion";
    public double bpm = 100;
    public double speed = 1.5;
    public final List<Note> notes = new ArrayList<>();

    public static Chart load(Path path) throws IOException {
        String text = Files.readString(path);
        if (text.startsWith("\uFEFF")) text = text.substring(1);
        JsonObject root = JsonParser.parseString(text).getAsJsonObject();
        JsonObject song = root.has("song") && root.get("song").isJsonObject() ? root.getAsJsonObject("song") : root;

        Chart c = new Chart();
        if (song.has("song") && song.get("song").isJsonPrimitive()) c.name = song.get("song").getAsString();
        if (song.has("bpm")) c.bpm = song.get("bpm").getAsDouble();
        if (song.has("speed")) c.speed = song.get("speed").getAsDouble();
        if (!song.has("notes") || !song.get("notes").isJsonArray()) throw new IOException("El chart no tiene 'notes'");

        for (JsonElement secEl : song.getAsJsonArray("notes")) {
            if (!secEl.isJsonObject()) continue;
            JsonObject sec = secEl.getAsJsonObject();
            boolean mustHit = sec.has("mustHitSection") && sec.get("mustHitSection").getAsBoolean();
            if (!sec.has("sectionNotes")) continue;
            for (JsonElement ne : sec.getAsJsonArray("sectionNotes")) {
                if (!ne.isJsonArray()) continue;
                JsonArray a = ne.getAsJsonArray();
                if (a.size() < 2) continue;
                double time = a.get(0).getAsDouble();
                int lane = (int) a.get(1).getAsDouble();
                if (lane < 0) continue; // eventos de Psych
                double sus = 0;
                if (a.size() > 2 && a.get(2).isJsonPrimitive() && a.get(2).getAsJsonPrimitive().isNumber()) sus = Math.max(0, a.get(2).getAsDouble());
                boolean player = mustHit ? lane < 4 : lane >= 4;
                c.notes.add(new Note(time, lane % 4, sus, player));
            }
        }
        c.notes.sort(Comparator.comparingDouble(n -> n.time));
        if (c.notes.isEmpty()) throw new IOException("El chart no tiene notas");
        return c;
    }
}
