package com.rengv.pixelgymfabric.gui;

import com.rengv.pixelgymfabric.PixelGymFabric;
import com.rengv.pixelgymfabric.Utils;
import com.rengv.pixelgymfabric.config.Config;
import com.rengv.pixelgymfabric.integration.CobbleSyncIntegration;
import com.rengv.pixelgymfabric.model.Badge;
import com.rengv.pixelgymfabric.storage.DbGetters;
import com.rengv.pixelgymfabric.storage.DbManager;
import com.rengv.pixelgymfabric.utils.ThreadUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class BadgesGUI extends ScreenHandler {
    private final ServerPlayerEntity player;

    private final SimpleInventory inventory = new SimpleInventory(27);

    public BadgesGUI(int syncId, PlayerInventory inv) {
        super(ScreenHandlerType.GENERIC_9X3, syncId);

        this.player = (ServerPlayerEntity) inv.player;

        for (int i = 0; i < 27; i++) {
            this.addSlot(new LockedSlot(inventory, i, 0, 0));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new LockedSlot(
                        inv,
                        col + row * 9 + 9,
                        8 + col * 18,
                        84 + row * 18
                ));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new LockedSlot(
                    inv,
                    col,
                    8 + col * 18,
                    142
            ));
        }
        
        build();
    }

    private void build() {
        int badgesObtained = DbGetters.getBadges(player.getUuid()).size();

        ItemStack redPanel = Utils.getItemStack("minecraft:red_stained_glass_pane", 1);
        redPanel.set(DataComponentTypes.CUSTOM_NAME, Utils.format(""));

        ItemStack whitePanel = Utils.getItemStack("minecraft:white_stained_glass_pane", 1);
        whitePanel.set(DataComponentTypes.CUSTOM_NAME, Utils.format(""));

        ItemStack leagueItem = Utils.getItemStack("minecraft:black_stained_glass_pane", 1);
        List<Text> loreLeagueItem = new ArrayList<>();
        if(DbGetters.isInLeague(player.getUuid())) {
            leagueItem = Utils.getItemStack("cobbleversebadges:kanto_league_trophy", 1);
            leagueItem.set(DataComponentTypes.CUSTOM_NAME, Utils.format("&6Estás inscrito en el torneo"));
            leagueItem.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        } else if(badgesObtained == 8) {
            leagueItem = Utils.getItemStack("minecraft:lime_stained_glass_pane", 1);
            leagueItem.set(DataComponentTypes.CUSTOM_NAME, Utils.format("&a¡Haz obtenido las 8 medallas!"));
            loreLeagueItem.add(Text.empty());
            loreLeagueItem.add(Utils.format("&eClick aquí para participar del torneo"));
        } else {
            leagueItem.set(DataComponentTypes.CUSTOM_NAME, Utils.format("&c&lX"));
            loreLeagueItem.add(Text.empty());
            loreLeagueItem.add(Utils.format("&8Te faltan: &e" + (8 - badgesObtained) + " medallas"));
        }
        leagueItem.set(DataComponentTypes.LORE, new LoreComponent(loreLeagueItem));


        for (int i = 0; i < 27; i++) {
            if(i < 9) this.slots.get(i).setStack(redPanel);
            else if (i == 13) this.slots.get(i).setStack(leagueItem);
            else if (i < 18){
                int gym = i - 8;
                if(i > 13) gym--;

                Badge badge = Config.badges.get(gym);

                if(badge == null) continue;

                ItemStack stack;

                List<Text> lore = new ArrayList<>();
                if (DbGetters.hasBadge(player.getUuid(), gym)) {
                    stack = Utils.getItemStack(badge.BadgeItem, 1);
                    stack.set(DataComponentTypes.CUSTOM_NAME, Utils.format(badge.BadgeDisplayName));

                    lore.add(Text.empty());
                    lore.add(Utils.format("&a¡Medalla obtenida!"));
                } else {
                    stack = Utils.getItemStack("minecraft:barrier", 1);
                    stack.set(DataComponentTypes.CUSTOM_NAME, Utils.format("&cTodavía no has obtenido la medalla " + badge.BadgeName));

                    lore.add(Text.empty());
                    lore.add(Utils.format("&e¡Click para desafiar al gimnasio!"));
                }

                stack.set(DataComponentTypes.LORE, new LoreComponent(lore));

                this.slots.get(i).setStack(stack);
            } else this.slots.get(i).setStack(whitePanel);
        }
    }
    @Override
    protected boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        return false;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void onSlotClick(int slot, int button, SlotActionType action, PlayerEntity player) {
        if(!(player instanceof ServerPlayerEntity sp)) return;
        int badgesObtained = DbGetters.getBadges(player.getUuid()).size();

        if(slot == 13) {
            if(DbGetters.isInLeague(player.getUuid())) player.sendMessage(Utils.format("&eYa estás inscrito en el torneo"));
            else if(badgesObtained == 8) {
                DbManager.addPlayerToLeague(player.getUuid(), player.getName().getString());
                ((ServerPlayerEntity) player).closeHandledScreen();
                player.sendMessage(Utils.format("&6Te has inscrito para participar del Torneo"));
                ((ServerPlayerEntity) player).networkHandler.sendPacket(new TitleFadeS2CPacket(10, 60, 10));
                ((ServerPlayerEntity) player).networkHandler.sendPacket(new TitleS2CPacket(Utils.format("&6¡Te has inscrito!")));
            } else {
                player.sendMessage(Utils.format("&cTodavía no has obtenido las 8 medallas"));
            }
        } else if (slot > 8 && slot < 18) {
            int gym = slot - 8;
            if(slot > 13) gym--;

            Badge badge = Config.badges.get(gym);
            if(badge == null) return;

            if(DbGetters.hasBadge(sp.getUuid(), gym)) {
                sp.sendMessage(Utils.format("&eYa tienes esta medalla"));
                return;
            }

            runActionCommands(sp, badge);
            build();
        }
    }

    private void runActionCommands(ServerPlayerEntity player, Badge badge) {
        if (badge == null || badge.CommandsAction == null) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        for (String cmd : badge.CommandsAction) {
            server.getCommandManager().executeWithPrefix(
                    server.getCommandSource(),
                    cmd.replace("%player%", player.getName().getString())
            );
        }
    }
}
