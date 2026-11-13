package com.example.reallife;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {
    /** Carries horizontal loads for free: spans through a beam do not count against the overhang limit. */
    public static final Block SUPPORT_BEAM = register("support_beam");

    private static Block register(String name) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, RealLifeMod.id(name));
        Block block = new RotatedPillarBlock(BlockBehaviour.Properties.of()
                .strength(2.0f, 6.0f).sound(SoundType.WOOD).setId(key));
        return Registry.register(BuiltInRegistries.BLOCK, key, block);
    }

    public static void register() {}
}
