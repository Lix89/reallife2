package com.example.reallife;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class ModItems {
    public static final Item SUPPORT_BEAM = Registry.register(BuiltInRegistries.ITEM,
            key("support_beam"),
            new BlockItem(ModBlocks.SUPPORT_BEAM,
                    new Item.Properties().setId(key("support_beam")).useBlockDescriptionPrefix()));

    public static final Item THERMOMETER = Registry.register(BuiltInRegistries.ITEM,
            key("thermometer"),
            new ThermometerItem(new Item.Properties().setId(key("thermometer")).stacksTo(1)));

    private static ResourceKey<Item> key(String name) {
        return ResourceKey.create(Registries.ITEM, RealLifeMod.id(name));
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS)
                .register(e -> e.accept(SUPPORT_BEAM));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(e -> e.accept(THERMOMETER));
    }
}
