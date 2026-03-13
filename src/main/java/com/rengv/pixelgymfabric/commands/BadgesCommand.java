package com.rengv.pixelgymfabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.rengv.pixelgymfabric.gui.BadgesGUI;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class BadgesCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        registerAlias(dispatcher, "medallas");
        registerAlias(dispatcher, "badges");
    }

    private static void registerAlias(CommandDispatcher<ServerCommandSource> dispatcher, String name) {
        dispatcher.register(
                literal(name)
                        .requires(src -> src.hasPermissionLevel(2))
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                                    (syncId, inv, p) -> new BadgesGUI(syncId, inv),
                                    Text.literal("              §8§lMedallas")
                            ));
                            return 1;
                        })
        );
    }
}