package com.example.reallife;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Structural stability. Every solid block gets a "span value":
 *  - natural ground = 0
 *  - a block resting on a supported block inherits its value
 *  - a block that is only attached sideways costs +1 per block of overhang
 *    (stepping out of a Support Beam costs 0).
 * Blocks above MAX_SPAN collapse into falling blocks.
 */
public class Stability {
    public static final int MAX_SPAN = 4;
    private static final int R = 10;          // horizontal scan radius
    private static final int SAFE = 5;        // only collapse blocks this close to the player (edges lack context)
    private static final int DOWN = 12, UP = 16;
    private static final int INF = 99;
    private static int tick = 0;

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (++tick % 40 != 0) return;
            for (ServerPlayer p : server.getPlayerList().getPlayers())
                scan((ServerLevel) p.level(), p.blockPosition());
        });
    }

    private static boolean solid(BlockState s) {
        return !s.isAir() && s.getFluidState().isEmpty() && s.blocksMotion();
    }

    private static boolean natural(BlockState s) {
        return s.is(BlockTags.DIRT) || s.is(BlockTags.BASE_STONE_OVERWORLD) || s.is(BlockTags.SAND)
                || s.is(Blocks.GRAVEL) || s.is(Blocks.BEDROCK) || s.is(Blocks.CLAY) || s.is(Blocks.SNOW_BLOCK);
    }

    public static void scan(ServerLevel level, BlockPos center) {
        int size = 2 * R + 1;
        int minY = Math.max(level.getMinY(), center.getY() - DOWN);
        int maxY = Math.min(level.getMaxY() - 1, center.getY() + UP);
        int[][] prev = new int[size][size];
        boolean[][] prevOk = new boolean[size][size];
        List<BlockPos> collapse = new ArrayList<>();
        BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();

        for (int y = minY; y <= maxY; y++) {
            int[][] val = new int[size][size];
            BlockState[][] st = new BlockState[size][size];
            for (int x = 0; x < size; x++)
                for (int z = 0; z < size; z++) {
                    mp.set(center.getX() - R + x, y, center.getZ() - R + z);
                    BlockState s = level.getBlockState(mp);
                    st[x][z] = s;
                    if (!solid(s)) { val[x][z] = -1; continue; }
                    if (natural(s)) { val[x][z] = 0; continue; }
                    if (y == minY) {
                        mp.set(mp.getX(), y - 1, mp.getZ());
                        val[x][z] = solid(level.getBlockState(mp)) ? 0 : INF;
                    } else {
                        val[x][z] = prevOk[x][z] ? prev[x][z] : INF;
                    }
                }
            // horizontal relaxation
            int[] dx = {1, -1, 0, 0}, dz = {0, 0, 1, -1};
            for (int pass = 0; pass < MAX_SPAN + 3; pass++) {
                boolean changed = false;
                for (int x = 0; x < size; x++)
                    for (int z = 0; z < size; z++) {
                        if (val[x][z] < 0 || natural(st[x][z])) continue;
                        for (int d = 0; d < 4; d++) {
                            int nx = x + dx[d], nz = z + dz[d];
                            if (nx < 0 || nz < 0 || nx >= size || nz >= size || val[nx][nz] < 0 || val[nx][nz] >= INF) continue;
                            int cost = st[nx][nz].is(ModBlocks.SUPPORT_BEAM) ? 0 : 1;
                            int cand = val[nx][nz] + cost;
                            if (cand < val[x][z]) { val[x][z] = cand; changed = true; }
                        }
                    }
                if (!changed) break;
            }
            boolean[][] ok = new boolean[size][size];
            for (int x = 0; x < size; x++)
                for (int z = 0; z < size; z++) {
                    if (val[x][z] < 0) continue;
                    if (val[x][z] <= MAX_SPAN) { ok[x][z] = true; continue; }
                    if (Math.abs(x - R) <= SAFE && Math.abs(z - R) <= SAFE
                            && Math.abs(y - center.getY()) <= UP - 1) {
                        BlockPos pos = new BlockPos(center.getX() - R + x, y, center.getZ() - R + z);
                        float hardness = st[x][z].getDestroySpeed(level, pos);
                        if (hardness >= 0 && !level.getBlockState(pos).hasBlockEntity()) collapse.add(pos);
                        // collapsing blocks no longer support what is above
                    } else {
                        ok[x][z] = true; // outside the trusted area: assume fine
                    }
                }
            prev = val;
            prevOk = ok;
        }

        int count = 0;
        for (BlockPos pos : collapse) {
            if (count++ >= 64) break;
            BlockState s = level.getBlockState(pos);
            if (!s.isAir()) FallingBlockEntity.fall(level, pos, s);
        }
    }
}
