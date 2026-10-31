package com.waosfnf.network;

import com.waosfnf.FnfMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class FnfNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(FnfMod.MODID, "main"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(NetworkRegistry.acceptMissingOr(PROTOCOL))
            .serverAcceptedVersions(NetworkRegistry.acceptMissingOr(PROTOCOL))
            .simpleChannel();

    private FnfNetwork() {}

    public static void register() {
        CHANNEL.messageBuilder(FnfControlPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(FnfControlPacket::encode)
                .decoder(FnfControlPacket::decode)
                .consumerMainThread(FnfControlPacket::handle)
                .add();
    }
}
