package com.example.reallife;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

public class RealLifeMod implements ModInitializer {
    public static final String MOD_ID = "reallife";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModItems.register();
        Needs.init();
        Stability.init();
    }
}
