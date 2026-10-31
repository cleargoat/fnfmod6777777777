package com.waosfnf.network;

import com.waosfnf.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Servidor -> cliente: iniciar (start=true, con nombre de cancion) o parar la cancion. */
public record FnfControlPacket(boolean start, String song) {
    public static void encode(FnfControlPacket m, FriendlyByteBuf buf) {
        buf.writeBoolean(m.start);
        buf.writeUtf(m.song, 256);
    }

    public static FnfControlPacket decode(FriendlyByteBuf buf) {
        return new FnfControlPacket(buf.readBoolean(), buf.readUtf(256));
    }

    public static void handle(FnfControlPacket m, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handle(m));
    }
}
