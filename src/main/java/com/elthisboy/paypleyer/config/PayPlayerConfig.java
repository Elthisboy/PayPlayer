package com.elthisboy.paypleyer.config;

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

public class PayPlayerConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("payplayer");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("payplayer.json");

    private static PayPlayerConfig INSTANCE;

    /** Internal scoreboard objective name (e.g. "money") */
    public String scoreboardName = "money";

    /** Currency display name (e.g. "Coins", "Dollars") */
    public String currencyName = "money";

    /** Symbol shown before amounts (e.g. "$", "€", "💰") */
    public String currencySymbol = "$";

    /**
     * If true, when charging a player the amount goes into the charger's account.
     * If false, the money simply disappears.
     */
    public boolean chargeToSelf = false;

    /**
     * If true, when giving money to a player the amount is deducted from the giver's account.
     * If false, the money is created out of thin air.
     */
    public boolean giveFromSelf = false;

    // ────────────────────────────────────────────────────────────────────────

    public static PayPlayerConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                INSTANCE = GSON.fromJson(reader, PayPlayerConfig.class);
                if (INSTANCE.scoreboardName == null) INSTANCE.scoreboardName = "money";
                if (INSTANCE.currencyName   == null) INSTANCE.currencyName   = "money";
                if (INSTANCE.currencySymbol == null) INSTANCE.currencySymbol = "$";
                LOGGER.info("[PayPlayer] Config loaded: scoreboard='{}', currency='{}'",
                        INSTANCE.scoreboardName, INSTANCE.currencyName);
            } catch (IOException e) {
                LOGGER.error("[PayPlayer] Error reading config, using defaults.", e);
                INSTANCE = new PayPlayerConfig();
            }
        } else {
            INSTANCE = new PayPlayerConfig();
            save();
            LOGGER.info("[PayPlayer] Config created at: {}", CONFIG_PATH);
        }
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            LOGGER.error("[PayPlayer] Error saving config.", e);
        }
    }
}
