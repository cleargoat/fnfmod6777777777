package com.waosfnf.command;

import com.waosfnf.FnfMod;
import com.waosfnf.network.FnfControlPacket;
import com.waosfnf.network.FnfNetwork;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Collection;

/**
 *  /fnfclear start <cancion> <jugadores>   (ej: /fnfclear start tutorial @a)
 *  /fnfclear stop <jugadores>              (ej: /fnfclear stop @s)
 *  Solo OP (nivel de permiso 2).
 */
@Mod.EventBusSubscriber(modid = FnfMod.MODID)
public class FnfCommands {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        var root = Commands.literal("fnfclear").requires(src -> src.hasPermission(2));
        for (String name : new String[]{"start", "Start"}) {
            root.then(Commands.literal(name)
                    .then(Commands.argument("song", StringArgumentType.string())
                            .then(Commands.argument("targets", EntityArgument.players())
                                    .executes(c -> run(c, true)))));
        }
        for (String name : new String[]{"stop", "Stop"}) {
            root.then(Commands.literal(name)
                    .then(Commands.argument("targets", EntityArgument.players())
                            .executes(c -> run(c, false))));
        }
        event.getDispatcher().register(root);
    }

    private static int run(CommandContext<CommandSourceStack> ctx, boolean start) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
        String song = start ? StringArgumentType.getString(ctx, "song") : "";
        FnfControlPacket packet = new FnfControlPacket(start, song);
        for (ServerPlayer p : targets) {
            FnfNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), packet);
        }
        int n = targets.size();
        ctx.getSource().sendSuccess(() -> Component.literal(start
                ? "FNF: iniciando '" + song + "' para " + n + " jugador(es)"
                : "FNF: musica detenida para " + n + " jugador(es)"), true);
        return n;
    }
}
