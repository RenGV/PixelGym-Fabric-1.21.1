package com.rengv.pixelgymfabric;

import com.rengv.pixelgymfabric.commands.BadgesCommand;
import com.rengv.pixelgymfabric.commands.MainCommand;
import com.rengv.pixelgymfabric.config.Config;
import com.rengv.pixelgymfabric.integration.CobbleSyncIntegration;
import com.rengv.pixelgymfabric.storage.DbManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PixelGymFabric implements ModInitializer {
    public static final String MOD_ID = "pixelgymfabric";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing PixelGymFabric");

        if(CobbleSyncIntegration.isSidemodAvailable()) {
            LOGGER.info("CobbleSync integration loaded successfully!");
        }

        try {
            DbManager.openConnectionSQLite();
        } catch (Exception e) {
            LOGGER.error("PixelGymFabric failed to start", e);
            return;
        }

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, enviroment) -> {
                    MainCommand.register(dispatcher);
                    BadgesCommand.register(dispatcher);
                }
        );

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Config.load();
            LOGGER.info("Badges loaded: " + Config.badges.keySet());
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            DbManager.close();
        });
    }
}
