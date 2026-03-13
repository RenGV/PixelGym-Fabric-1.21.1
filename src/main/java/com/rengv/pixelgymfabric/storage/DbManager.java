package com.rengv.pixelgymfabric.storage;

import com.rengv.pixelgymfabric.PixelGymFabric;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.UUID;

public class DbManager {
    public static Connection con;

    public static void openConnectionSQLite() throws SQLException {
        if (con != null) return;

        try {
            Path modDir = FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("pixelgym");

            Files.createDirectories(modDir);

            Path dbFile = modDir.resolve("pixelgym.db");
            String url = "jdbc:sqlite:" + dbFile.toAbsolutePath();

            con = DriverManager.getConnection(url);

            PixelGymFabric.LOGGER.info("SQLite connection started");

            createTable();

        } catch (Exception e) {
            throw new SQLException("Failed to open SQLite connection", e);
        }
    }

    public static Connection get() {
        if (con == null) {
            throw new IllegalStateException("Database not initialized");
        }
        return con;
    }

    public static void createTable() {
        try (Statement stmt = con.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS GYMCLAIMS (
                    GUID TEXT NOT NULL,
                    GYM INTEGER NOT NULL,
                    RIDE BOOLEAN NOT NULL DEFAULT false,
                    PRIMARY KEY (GUID)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS PLAYERBADGES (
                    BUID TEXT NOT NULL,
                    BADGE BOOLEAN NOT NULL DEFAULT false,
                    NAME TEXT NOT NULL,
                    GYM INTEGER NOT NULL,
                    PUID TEXT NOT NULL,
                    PRIMARY KEY (BUID)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS LEAGUE (
                    PUID TEXT NOT NULL,
                    NAME TEXT NOT NULL,
                    PRIMARY KEY (PUID)
                )
            """);

            PixelGymFabric.LOGGER.info("Database tables ensured");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void close() {
        if (con != null) {
            try {
                con.close();
                con = null;
                PixelGymFabric.LOGGER.info("SQLite connection closed");
            } catch (SQLException e) {
                PixelGymFabric.LOGGER.error("Failed to close SQLite connection", e);
            }
        }
    }

    public static void clearDB() {
        Connection con = get();

        try {
            Statement stmt = con.createStatement();
            stmt.execute("DELETE FROM GYMCLAIMS");
            stmt.execute("DELETE FROM PLAYERBADGES");
            stmt.execute("DELETE FROM LEAGUE");
        } catch (SQLException e) {
            PixelGymFabric.LOGGER.error("Failed to clear database", e);
        }
    }

    public static void resetPlayer(UUID player) {
        Connection con = get();

        try (
            PreparedStatement psBadges = con.prepareStatement("DELETE FROM PLAYERBADGES WHERE PUID = ?");
            PreparedStatement psLeague = con.prepareStatement("DELETE FROM LEAGUE WHERE PUID = ?");
        ) {
            psBadges.setString(1, player.toString());
            psBadges.executeUpdate();
            psLeague.setString(1, player.toString());
            psLeague.executeUpdate();
        } catch (SQLException e) {
            PixelGymFabric.LOGGER.error("Failed to reset player " + player, e);
        }
    }

    public static void resetPlayers() {
        Connection con = get();

        try (Statement stmt = con.createStatement()){
            stmt.execute("DELETE FROM PLAYERBADGES");
            stmt.execute("DELETE FROM LEAGUE");
        } catch (SQLException e) {
            PixelGymFabric.LOGGER.error("Failed to reset players", e);
        }
    }

    public static void setBadge(UUID player, boolean isTrue, String name, int gym) {
        Connection con = get();

        try(PreparedStatement ps = con.prepareStatement("INSERT INTO PLAYERBADGES (BUID, BADGE, NAME, GYM, PUID) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, player.toString() + gym);
            ps.setBoolean(2, isTrue);
            ps.setString(3, name);
            ps.setInt(4, gym);
            ps.setString(5, player.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            PixelGymFabric.LOGGER.error("Failed to set badge", e);
        }
    }

    public static void removeBadge(UUID player, int gym) {
        Connection con = get();

        try(PreparedStatement ps = con.prepareStatement("DELETE FROM PLAYERBADGES WHERE BUID = ?")) {
            ps.setString(1, player.toString() + gym);
            ps.executeUpdate();
        } catch (SQLException e) {
            PixelGymFabric.LOGGER.error("Failed to set badge", e);
        }
    }

    public static void addPlayerToLeague(UUID player, String name) {
        Connection con = get();

        try(PreparedStatement ps = con.prepareStatement("INSERT INTO LEAGUE (PUID, NAME) VALUES (?, ?)")){
            ps.setString(1, player.toString());
            ps.setString(2, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            PixelGymFabric.LOGGER.error("Failed to add player to league", e);
        }
    }
}
