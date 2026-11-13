package com.example.reallife;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Estimates a temperature in degrees Celsius for any position. */
public class Climate {
    public static final int HEAT_RADIUS = 4;

    public static boolean isIndoors(Level level, BlockPos pos) {
        return !level.canSeeSky(pos.above());
    }

    public static double outdoor(Level level, BlockPos pos) {
        double base = -5.0 + level.getBiome(pos).value().getBaseTemperature() * 34.0;
        long t = level.getDayTime() % 24000L;
        if (t > 13000L && t < 23000L) base -= 7.0; // night
        return Math.max(-30.0, Math.min(45.0, base));
    }

    public static double heatFromSources(Level level, BlockPos pos) {
        double heat = 0;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dx = -HEAT_RADIUS; dx <= HEAT_RADIUS; dx++)
            for (int dy = -2; dy <= 2; dy++)
                for (int dz = -HEAT_RADIUS; dz <= HEAT_RADIUS; dz++) {
                    p.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    BlockState s = level.getBlockState(p);
                    boolean lit = s.hasProperty(BlockStateProperties.LIT) && s.getValue(BlockStateProperties.LIT);
                    if (lit || s.is(Blocks.LAVA) || s.is(Blocks.FIRE)) heat += 3.0;
                }
        return Math.min(heat, 20.0);
    }

    public static double at(Level level, BlockPos pos) {
        double out = outdoor(level, pos);
        double temp = out;
        if (isIndoors(level, pos)) temp = out + (20.0 - out) * 0.5; // walls and roof insulate
        return temp + heatFromSources(level, pos);
    }
}
