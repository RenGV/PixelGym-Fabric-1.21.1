package com.rengv.pixelgymfabric.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.UUID;

public class DbGetters {
    public static boolean hasBadge(UUID player, int badge) {
        Connection con = DbManager.get();
        String sql = "SELECT BADGE FROM PLAYERBADGES WHERE BUID = ?";

        try(PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, player.toString() + badge);

            try (ResultSet rs = ps.executeQuery()){
                return rs.next() && rs.getBoolean("BADGE");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check badge", e);
        }
    }

    public static LinkedHashMap<String, Boolean> getBadges(UUID player) {
        Connection con = DbManager.get();
        String sql = "SELECT NAME, BADGE FROM PLAYERBADGES WHERE PUID = ? ORDER BY GYM ASC";
        LinkedHashMap<String, Boolean> list = new LinkedHashMap();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, player.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.put(rs.getString("NAME"), rs.getBoolean("BADGE"));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get badges", e);
        }

        return list;
    }

    public static LinkedHashMap<UUID, Integer> getTop() {
        Connection con = DbManager.get();
        String sql = "SELECT PUID, COUNT(*) AS TOTAL FROM PLAYERBADGES WHERE BADGE = TRUE GROUP BY PUID ORDER BY TOTAL DESC";
        LinkedHashMap<UUID, Integer> list = new LinkedHashMap();

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.put(
                        UUID.fromString(rs.getString("PUID")),
                        rs.getInt("TOTAL")
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get top", e);
        }

        return list;
    }

    public static boolean isInLeague(UUID player) {
        Connection con = DbManager.get();
        String sql = "SELECT 1 FROM LEAGUE WHERE PUID = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, player.toString());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check league", e);
        }
    }

    public static LinkedHashMap<UUID, String> getLeague() {
        Connection con = DbManager.get();
        String sql = "SELECT PUID, NAME FROM LEAGUE";
        LinkedHashMap<UUID, String> list = new LinkedHashMap();

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.put(
                        UUID.fromString(rs.getString("PUID")),
                        rs.getString("NAME")
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get league", e);
        }

        return list;
    }
}
