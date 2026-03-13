package com.rengv.pixelgymfabric.integration;

import net.minecraft.entity.player.PlayerEntity;

import java.lang.reflect.Method;

public class CobbleSyncIntegration {
    private static boolean sidemodAvailable = false;
    private static Method saveBadge;
    private static Method removeBadge;
    private static Method resetBadge;
    private static Method resetBadgeForPlayer;

    static {
        try {
            Class<?> apiClass = Class.forName("com.rengv.cobblesync.storage.DbManager");
            saveBadge = apiClass.getMethod("saveBadge", String.class, int.class);
            removeBadge = apiClass.getMethod("removeBadge", String.class, int.class);
            resetBadge = apiClass.getMethod("resetBadge");
            resetBadgeForPlayer = apiClass.getMethod("resetBadge", String.class);
            sidemodAvailable = true;
        } catch (Exception e) {
            sidemodAvailable = false;
        }
    }

    public static boolean isSidemodAvailable() {
        return sidemodAvailable;
    }

    public static boolean saveBadgeCobbleSync(PlayerEntity player, int gym) {
        boolean result = false;

        try {
            String username = player.getName().getString();
            result = (Boolean) saveBadge.invoke(null, username, gym);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static boolean removeBadgeCobbleSync(PlayerEntity player, int gym) {
        boolean result = false;

        try {
            String username = player.getName().getString();
            result = (Boolean) removeBadge.invoke(null, username, gym);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static boolean resetBadgeCobbleSync() {
        boolean result = false;

        try {
            result = (Boolean) resetBadge.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static boolean resetBadgeCobbleSync(String username) {
        boolean result = false;

        try {
            result = (Boolean) resetBadgeForPlayer.invoke(null, username);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
