package com.example.mahitosoulregen;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(MahitoSoulRegen.MOD_ID)
public class MahitoSoulRegen {
    public static final String MOD_ID = "mahitosoulregen";

    public MahitoSoulRegen() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, RegenConfig.SPEC);
    }
}
