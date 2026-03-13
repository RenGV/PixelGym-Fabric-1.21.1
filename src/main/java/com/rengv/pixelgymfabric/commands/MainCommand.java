package com.rengv.pixelgymfabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.rengv.pixelgymfabric.PixelGymFabric;
import com.rengv.pixelgymfabric.Utils;
import com.rengv.pixelgymfabric.config.Config;
import com.rengv.pixelgymfabric.integration.CobbleSyncIntegration;
import com.rengv.pixelgymfabric.model.Badge;
import com.rengv.pixelgymfabric.storage.DbGetters;
import com.rengv.pixelgymfabric.storage.DbManager;
import com.rengv.pixelgymfabric.utils.ThreadUtil;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MainCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("pixelgym")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> {
                            context.getSource().sendMessage(Text.literal("§6=== §4Pixel§fGym §6==="));
                            context.getSource().sendMessage(Text.literal("§e/pixelgym top"));
                            context.getSource().sendMessage(Text.literal("§e/pixelgym league"));
                            context.getSource().sendMessage(Text.literal("§e/pixelgym badge add <player> <gym>"));
                            context.getSource().sendMessage(Text.literal("§e/pixelgym badge remove <player> <gym>"));
                            context.getSource().sendMessage(Text.literal("§e/pixelgym league"));
                            context.getSource().sendMessage(Text.literal("§e/pixelgym reset <player>"));
                            context.getSource().sendMessage(Text.literal("§e/pixelgym resetall"));
                            context.getSource().sendMessage(Text.literal("§e/pixelgym reload"));
                            return 1;
                        })
                        .then(literal("reload")
                                .executes(ctx -> {
                                    Config.load();
                                    ctx.getSource().sendMessage(Text.literal("§aPixelGymFabric reloaded!"));
                                    return 1;
                                })
                        )
                        .then(literal("league")
                                .executes(ctx -> showLeague(ctx.getSource()))
                        )

                        .then(literal("badge")
                                // /pixelgym badge add <player> <gym>
                                .then(literal("add")
                                        .then(argument("player", EntityArgumentType.player())
                                                .then(argument("gym", IntegerArgumentType.integer(1))
                                                        .executes(ctx -> {
                                                            ServerCommandSource source = ctx.getSource();
                                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
                                                            int gym = IntegerArgumentType.getInteger(ctx, "gym");

                                                            if (!Config.badges.containsKey(gym)) {
                                                                source.sendError(Text.literal("§cEl gym especificado no existe."));
                                                                return 0;
                                                            }

                                                            if(DbGetters.hasBadge(player.getUuid(), gym)) {
                                                                source.sendError(Text.literal("§cEl jugador ya tiene esta medalla."));
                                                                return 0;
                                                            }

                                                            addBadge(player, gym);

                                                            source.sendMessage(Text.literal(
                                                                    "§aSe otorgó la medalla §e#" + gym + " §aa §b" + player.getName().getString()
                                                            ));

                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                                .then(literal("remove")
                                        .then(argument("player", EntityArgumentType.player())
                                                .then(argument("gym", IntegerArgumentType.integer(1))
                                                        .executes(ctx -> {
                                                            ServerCommandSource source = ctx.getSource();
                                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
                                                            int gym = IntegerArgumentType.getInteger(ctx, "gym");

                                                            if (!Config.badges.containsKey(gym)) {
                                                                source.sendError(Text.literal("§cEl gym especificado no existe."));
                                                                return 0;
                                                            }

                                                            if(!DbGetters.hasBadge(player.getUuid(), gym)) {
                                                                source.sendError(Text.literal("§cEl jugador no tiene esta medalla."));
                                                                return 0;
                                                            }

                                                            removeBadge(player, gym);

                                                            source.sendMessage(Text.literal(
                                                                    "§aSe eliminó la medalla §e#" + gym + " §aa §b" + player.getName().getString()
                                                            ));

                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                        )

                        // /pixelgym reset <player>
                        .then(literal("reset")
                                .then(argument("player", StringArgumentType.word())
                                        .executes(ctx ->
                                                resetPlayer(
                                                        ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "player")
                                                )
                                        )
                                )
                        )

                        // /pixelgym resetall
                        .then(literal("resetall")
                                .executes(ctx -> {
                                    DbManager.clearDB();
                                    ctx.getSource().sendMessage(Text.literal(
                                            "§eSe ha restaurado la base de datos por completo."
                                    ));

                                    if(CobbleSyncIntegration.isSidemodAvailable()) {
                                        boolean res = CobbleSyncIntegration.resetBadgeCobbleSync();
                                        if(res) ctx.getSource().sendMessage(Utils.format("&eSe ha restaurado los datos de los jugadores en la base de datos de CobbleSync."));
                                    }
                                    return 1;
                                })
                        )
                        .then(literal("top")
                                .executes(ctx -> showTop(ctx.getSource(), 1))
                                .then(argument("page", IntegerArgumentType.integer(1))
                                        .executes(ctx ->
                                                showTop(ctx.getSource(),
                                                        IntegerArgumentType.getInteger(ctx, "page")
                                                )
                                        )
                                )
                        )
        );
    }

    private static int showTop(ServerCommandSource source, int page) {
        LinkedHashMap<UUID, Integer> top = DbGetters.getTop();

        int linesPerPage = 5;
        int start = (page - 1) * linesPerPage;
        int end = Math.min(start + linesPerPage, top.size());

        source.sendMessage(Text.literal("§9===== PixelGym TOP ====="));

        int index = 1;
        for (Map.Entry<UUID, Integer> entry : top.entrySet()) {
            if (index - 1 < start || index - 1 >= end) {
                index++;
                continue;
            }

            String name = source.getServer()
                    .getUserCache()
                    .getByUuid(entry.getKey())
                    .map(p -> p.getName())
                    .orElse("Unknown");

            source.sendMessage(Text.literal(
                    "§8[" + index + "] §e" + name + " §b- Medallas: §a" + entry.getValue()
            ));
            index++;
        }

        // Botón siguiente
        source.sendMessage(
                Text.literal("§b[→]")
                        .setStyle(Style.EMPTY.withClickEvent(
                                new ClickEvent(
                                        ClickEvent.Action.RUN_COMMAND,
                                        "/pixelgym top " + (page + 1)
                                )
                        ))
        );

        return 1;
    }

    private static void addBadge(ServerPlayerEntity player, int gym) {
        Badge badge = Config.badges.get(gym);

        DbManager.setBadge(player.getUuid(), true, badge.BadgeName, gym);

        if(CobbleSyncIntegration.isSidemodAvailable()){
            ThreadUtil.runAsync(() -> {
                boolean res = CobbleSyncIntegration.saveBadgeCobbleSync(player, gym);
                if(res) PixelGymFabric.LOGGER.info("Player Data saved in CobbleSync DataBase for " + player.getName().getString());
            });
        }

        player.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 60, 10));
        player.networkHandler.sendPacket(new TitleS2CPacket(Utils.format("&a¡Medalla guardada!")));

        runRewardCommands(player, badge);
    }

    private static void removeBadge(ServerPlayerEntity player, int gym) {
        DbManager.removeBadge(player.getUuid(), gym);

        if(CobbleSyncIntegration.isSidemodAvailable()){
            ThreadUtil.runAsync(() -> {
                boolean res = CobbleSyncIntegration.removeBadgeCobbleSync(player, gym);
                if(res) PixelGymFabric.LOGGER.info("Player Data saved in CobbleSync DataBase for " + player.getName().getString());
            });
        }
    }

    private static int showLeague(ServerCommandSource source) {
        LinkedHashMap<UUID, String> league = DbGetters.getLeague();

        source.sendMessage(Text.literal("§9===== PixelGym LEAGUE ====="));

        int index = 1;
        for (Map.Entry<UUID, String> entry : league.entrySet()) {
            source.sendMessage(Text.literal(
                    "§8[" + index + "] §e" + entry.getValue()
            ));
            index++;
        }

        if (league.isEmpty()) {
            source.sendMessage(Text.literal("§cNo hay jugadores inscritos en la liga."));
        }

        return 1;
    }

    private static int resetPlayer(ServerCommandSource source, String playerName) {
        MinecraftServer server = source.getServer();

        UUID uuid = server.getUserCache()
                .findByName(playerName)
                .map(profile -> profile.getId())
                .orElse(null);

        if (uuid == null) {
            source.sendError(Text.literal("§cEl jugador no existe o nunca ha entrado al servidor."));
            return 0;
        }

        if(CobbleSyncIntegration.isSidemodAvailable()) {
            boolean res = CobbleSyncIntegration.resetBadgeCobbleSync(playerName);
            if(res) source.sendMessage(Utils.format("&eSe ha restaurado los datos de &3" + playerName + " &een la base de datos de CobbleSync."));
        }

        DbManager.resetPlayer(uuid);

        source.sendMessage(Text.literal(
                "§eSe ha restaurado la información de §3" + playerName + "§e."
        ));
        return 1;
    }

    private static void runRewardCommands(ServerPlayerEntity player, Badge badge) {
        if (badge == null || badge.CommandsReward == null) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        for (String cmd : badge.CommandsReward) {
            server.getCommandManager().executeWithPrefix(
                    server.getCommandSource(),
                    cmd.replace("%player%", player.getName().getString())
            );
        }
    }
}
