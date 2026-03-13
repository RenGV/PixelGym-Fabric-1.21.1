package com.rengv.pixelgymfabric;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Utils {
    public static Text format(String text) {
        return Text.literal(text.replace("&", "§"));
    }

    public static ItemStack getItemStack(String id, int quantity) {
        Identifier identifier = Identifier.tryParse(id);
        if(identifier == null){
            return ItemStack.EMPTY;
        }

        Item item = Registries.ITEM.get(identifier);

        if(item == null || item == Registries.ITEM.get(Identifier.of("minecraft", "air"))){
            return ItemStack.EMPTY;
        }

        return new ItemStack(item, quantity);
    }
}
