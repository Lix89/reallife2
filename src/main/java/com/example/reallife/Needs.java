package com.example.reallife;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Thirst and temperature survival. Thirst is kept in memory (resets on server restart). */
public class Needs {
    private static final Map<UUID, Float> THIRST = new HashMap<>();
    private static int tick = 0;

    public static float getThirst(UUID id) {
        return THIRST.getOrDefault(id, 100f);
    }

    public static void init() {
        // Drink a potion/water bottle
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (!level.isClientSide() && player.getItemInHand(hand).is(Items.POTION))
                THIRST.put(player.getUUID(), Math.min(100f, getThirst(player.getUUID()) + 35f));
            return InteractionResult.PASS;
        });
        // Sneak + empty hand on water: drink from it
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (!level.isClientSide() && player.isShiftKeyDown() && player.getMainHandItem().isEmpty()
                    && level.getFluidState(hit.getBlockPos()).is(Fluids.WATER)) {
                THIRST.put(player.getUUID(), Math.min(100f, getThirst(player.getUUID()) + 15f));
                player.displayClientMessage(Component.literal("You drink from the water."), true);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (++tick % 20 != 0) return;
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                if (p.isCreative() || p.isSpectator() || !p.isAlive()) continue;
                ServerLevel level = (ServerLevel) p.level();
                double temp = Climate.at(level, p.blockPosition());

                float thirst = getThirst(p.getUUID());
                float drain = 0.12f + (p.isSprinting() ? 0.25f : 0f) + (temp > 32 ? 0.25f : 0f);
                thirst = Math.max(0f, thirst - drain);
                THIRST.put(p.getUUID(), thirst);

                if (thirst <= 0f && tick % 80 == 0)
                    p.hurtServer(level, level.damageSources().generic(), 1.0f);
                if (temp < -2.0 && tick % 80 == 0)
                    p.hurtServer(level, level.damageSources().freeze(), 1.0f);
                if (temp < 5.0 || temp > 38.0 || thirst < 30f) {
                    String msg = temp < 5.0 ? "You are freezing. Find a heated room!"
                            : temp > 38.0 ? "It is dangerously hot." : "You are very thirsty.";
                    p.displayClientMessage(Component.literal(msg), true);
                }
            }
        });
    }
}
