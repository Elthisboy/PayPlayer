package com.elthisboy.paypleyer.pvpmoney;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class PvpMoneyConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("payplayer");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("pvp_money.json");

    private static PvpMoneyConfig INSTANCE;

    public boolean enabled = true;
    public String scoreboardObjective = "money";
    public double deathLosePercent = 0.20;
    public double killerStealPercent = 0.10;
    public boolean sendMessages = true;
    public boolean playSound = true;

    public static PvpMoneyConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                INSTANCE = GSON.fromJson(reader, PvpMoneyConfig.class);
                if (INSTANCE.scoreboardObjective == null) INSTANCE.scoreboardObjective = "money";
                INSTANCE.deathLosePercent = clampPercent(INSTANCE.deathLosePercent);
                INSTANCE.killerStealPercent = clampPercent(INSTANCE.killerStealPercent);
                LOGGER.info("[PayPlayer] PvP Money config loaded: enabled={}, objective='{}', deathLose={}%, killerSteal={}%",
                        INSTANCE.enabled, INSTANCE.scoreboardObjective,
                        (int) (INSTANCE.deathLosePercent * 100),
                        (int) (INSTANCE.killerStealPercent * 100));
            } catch (IOException e) {
                LOGGER.error("[PayPlayer] Error reading PvP Money config, using defaults.", e);
                INSTANCE = new PvpMoneyConfig();
            }
        } else {
            INSTANCE = new PvpMoneyConfig();
            save();
            LOGGER.info("[PayPlayer] PvP Money config created at: {}", CONFIG_PATH);
        }
    }

    public static void reload() {
        load();
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            LOGGER.error("[PayPlayer] Error saving PvP Money config.", e);
        }
    }

    private static double clampPercent(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
