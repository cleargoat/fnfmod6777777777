package com.waosfnf;

import com.waosfnf.network.FnfNetwork;
import net.minecraftforge.fml.common.Mod;

@Mod(FnfMod.MODID)
public class FnfMod {
    public static final String MODID = "waosfnf";

    public FnfMod() {
        FnfNetwork.register();
    }
}
