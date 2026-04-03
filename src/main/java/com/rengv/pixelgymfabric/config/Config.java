package com.rengv.pixelgymfabric.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rengv.pixelgymfabric.PixelGymFabric;
import com.rengv.pixelgymfabric.model.Badge;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private Config() {}

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir()
            .resolve("pixelgym")
            .resolve("Badges.json");

    public static Map<Integer, Badge> badges = new HashMap<>();

    private static class ConfigData {
        Map<String, Badge> badges;
    }

    public static void load() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                saveDefault();
                PixelGymFabric.LOGGER.info("Badges config created");
            }

            ConfigData data = GSON.fromJson(
                    Files.readString(CONFIG_PATH),
                    ConfigData.class
            );

            if (data != null && data.badges != null) {
                apply(data);
            }

        } catch (Exception e) {
            PixelGymFabric.LOGGER.error("Failed to load Badges.json, using defaults", e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(
                    CONFIG_PATH,
                    GSON.toJson(toData())
            );
        } catch (Exception e) {
            PixelGymFabric.LOGGER.error("Failed to save Badges.json", e);
        }
    }

//    public static void reload() {
//        load();
//        PixelGymFabric.LOGGER.info("Badges config reloaded");
//    }

    private static void apply(ConfigData d) {
        badges.clear();

        for (Map.Entry<String, Badge> entry : d.badges.entrySet()) {
            try {
                int id = Integer.parseInt(entry.getKey());
                badges.put(id, entry.getValue());
            } catch (NumberFormatException ignored) {}
        }
    }

    private static ConfigData toData() {
        ConfigData d = new ConfigData();

        Map<String, Badge> map = new HashMap<>();
        for (Map.Entry<Integer, Badge> e : badges.entrySet()) {
            map.put(String.valueOf(e.getKey()), e.getValue());
        }

        d.badges = map;
        return d;
    }

    private static void saveDefault() throws Exception {

        Files.createDirectories(CONFIG_PATH.getParent());

        String defaultJson = """
        {
           "badges": {
             "1": {
               "MinLevel": 30,
               "GymName": "&8&lGimnasio Roca",
               "BadgeName": "Roca",
               "BadgeDisplayName": "&8&lMedalla Roca",
               "BadgeItem": "cobbleversebadges:kanto_boulder_badge",
               "CommandsReward": [
                 "pixelpass addexp %player% 15"
               ],
               "CommandsAction": []
             },
             "2": {
               "MinLevel": 40,
               "GymName": "&9&lGimnasio Agua",
               "BadgeName": "Cascada",
               "BadgeDisplayName": "&9&lMedalla Cascada",
               "BadgeItem": "cobbleversebadges:kanto_cascade_badge",
               "CommandsReward": [
                 "pixelpass addexp %player% 15"
               ],
               "CommandsAction": []
             },
             "3": {
               "MinLevel": 50,
               "GymName": "&e&lGimnasio Eléctrico",
               "BadgeName": "Trueno",
               "BadgeDisplayName": "&e&lMedalla Trueno",
               "BadgeItem": "cobbleversebadges:kanto_thunder_badge",
               "CommandsReward": [
                 "pixelpass addexp %player% 15"
               ],
               "CommandsAction": []
             },
             "4": {
               "MinLevel": 60,
               "GymName": "&2&lGimnasio Planta",
               "BadgeName": "Arcoíris",
               "BadgeDisplayName": "&2&lMedalla Arcoíris",
               "BadgeItem": "cobbleversebadges:kanto_rainbow_badge",
               "CommandsReward": [
                 "pixelpass addexp %player% 15"
               ],
               "CommandsAction": []
             },
             "5": {
               "MinLevel": 70,
               "GymName": "&5&lGimnasio Veneno",
               "BadgeName": "Alma",
               "BadgeDisplayName": "&5&lMedalla Alma",
               "BadgeItem": "cobbleversebadges:kanto_soul_badge",
               "CommandsReward": [
                 "pixelpass addexp %player% 15"
               ],
               "CommandsAction": []
             },
             "6": {
               "MinLevel": 80,
               "GymName": "&d&lGimnasio Psíquico",
               "BadgeName": "Pantano",
               "BadgeDisplayName": "&d&lMedalla Pantano",
               "BadgeItem": "cobbleversebadges:kanto_marsh_badge",
               "CommandsReward": [
                 "pixelpass addexp %player% 15"
               ],
               "CommandsAction": []
             },
             "7": {
               "MinLevel": 90,
               "GymName": "&4&lGimnasio Fuego",
               "BadgeName": "Volcán",
               "BadgeDisplayName": "&4&lMedalla Volcán",
               "BadgeItem": "cobbleversebadges:kanto_volcano_badge",
               "CommandsReward": [
                 "pixelpass addexp %player% 15"
               ],
               "CommandsAction": []
             },
             "8": {
               "MinLevel": 100,
               "GymName": "&6&lGimnasio Tierra",
               "BadgeName": "Tierra",
               "BadgeDisplayName": "&6&lMedalla Tierra",
               "BadgeItem": "cobbleversebadges:kanto_earth_badge",
               "CommandsReward": [
                 "pixelpass addexp %player% 15"
               ],
               "CommandsAction": []
             }
           }
         }
        """;

        Files.writeString(CONFIG_PATH, defaultJson);
    }
}
