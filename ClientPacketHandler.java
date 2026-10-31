package com.waosfnf.client;

import com.waosfnf.game.Chart;
import com.waosfnf.game.FnfSongs;
import com.waosfnf.network.FnfControlPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** Solo se carga en el cliente (via DistExecutor). */
public final class ClientPacketHandler {
    private ClientPacketHandler() {}

    public static void handle(FnfControlPacket msg) {
        Minecraft mc = Minecraft.getInstance();
        if (!msg.start()) {
            if (mc.screen instanceof FnfPlayScreen play) play.onClose();
            return;
        }
        FnfSongs.Entry entry = FnfSongs.find(msg.song());
        if (entry == null) {
            say(mc, "No se encontro la cancion '" + msg.song() + "' en la carpeta fnf/songs");
            return;
        }
        try {
            Chart chart = Chart.load(entry.chart());
            mc.getMusicManager().stopPlaying();
            mc.setScreen(new FnfPlayScreen(null, entry, chart));
        } catch (Exception e) {
            say(mc, "Error al leer el chart: " + e.getMessage());
        }
    }

    private static void say(Minecraft mc, String text) {
        if (mc.player != null) mc.player.displayClientMessage(Component.literal("[FNF] " + text), false);
    }
}
