package com.example.reallife;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class ThermometerItem extends Item {
    public ThermometerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            double here = Climate.at(level, player.blockPosition());
            double out = Climate.outdoor(level, player.blockPosition());
            String where = Climate.isIndoors(level, player.blockPosition()) ? "indoors" : "outside";
            player.displayClientMessage(Component.literal(String.format(
                    "Temperature here (%s): %.1f C | Outdoor: %.1f C | Thirst: %d%%",
                    where, here, out, Math.round(Needs.getThirst(player.getUUID())))), true);
        }
        return InteractionResult.SUCCESS;
    }
}
