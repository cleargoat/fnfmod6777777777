package com.waosfnf.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.nio.file.Path;

/** Configuracion del jugador (config/waosfnf-client.json). */
public final class FnfConfig {
    public static final int[] DEFAULT_KEYS = {GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_RIGHT};

    /** Orden de carriles: 0=izquierda, 1=abajo, 2=arriba, 3=derecha. */
    public static final int[] keys = DEFAULT_KEYS.clone();
    public static boolean downscroll = false;
    /** false = flechas originales de FNF, true = flechas pixeladas. */
    public static boolean pixelArrows = false;

    private FnfConfig() {}

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve("waosfnf-client.json");
    }

    public static void resetKeys() {
        System.arraycopy(DEFAULT_KEYS, 0, keys, 0, 4);
    }

    public static void load() {
        try {
            Path f = file();
            if (!Files.exists(f)) { save(); return; }
            JsonObject o = JsonParser.parseString(Files.readString(f)).getAsJsonObject();
            if (o.has("downscroll")) downscroll = o.get("downscroll").getAsBoolean();
            if (o.has("pixelArrows")) pixelArrows = o.get("pixelArrows").getAsBoolean();
            if (o.has("keys")) {
                JsonArray a = o.getAsJsonArray("keys");
                for (int i = 0; i < 4 && i < a.size(); i++) keys[i] = a.get(i).getAsInt();
            }
        } catch (Exception e) {
            LogUtils.getLogger().warn("[WaosFNF] No se pudo leer la config, usando valores por defecto", e);
        }
    }

    public static void save() {
        try {
            JsonObject o = new JsonObject();
            o.addProperty("downscroll", downscroll);
            o.addProperty("pixelArrows", pixelArrows);
            JsonArray a = new JsonArray();
            for (int k : keys) a.add(k);
            o.add("keys", a);
            Files.writeString(file(), new GsonBuilder().setPrettyPrinting().create().toJson(o));
        } catch (Exception e) {
            LogUtils.getLogger().warn("[WaosFNF] No se pudo guardar la config", e);
        }
    }
}
