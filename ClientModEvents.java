package com.waosfnf.client;

import com.waosfnf.FnfMod;
import com.waosfnf.game.FnfConfig;
import com.waosfnf.game.FnfSongs;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = FnfMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    /** Tecla U: abre el menu de FNF. */
    public static final KeyMapping MENU_KEY = new KeyMapping("key.waosfnf.menu", GLFW.GLFW_KEY_U, "key.categories.waosfnf");

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(MENU_KEY);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            FnfSongs.ensureFolders();
            FnfConfig.load();
        });
    }
}
