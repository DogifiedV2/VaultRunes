package com.dogv2.vaultrunes.config;

import com.dogv2.vaultrunes.VaultRunes;
import com.dogv2.vaultrunes.item.VaultRune;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class RuneRollConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_DIR = "vault_runes";
    private static final String ROLL_CONFIG_FILE = "rune_rolls.json";

    private static Map<Integer, Integer> tierWeights = new HashMap<>();
    private static Map<VaultRune.RuneGod, Integer> godWeights = new HashMap<>();
    private static Map<VaultRune.RuneType, Integer> typeWeights = new HashMap<>();

    public static void init() {
        try {
            Path configDir = FMLPaths.CONFIGDIR.get().resolve(CONFIG_DIR);
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            File configFile = new File(configDir.toFile(), ROLL_CONFIG_FILE);
            if (!configFile.exists()) {
                VaultRunes.LOGGER.info("Creating default config file at {}", configFile);
                createDefaultConfig(configFile);
            } else {
                VaultRunes.LOGGER.info("Loading existing config file from {}", configFile);
                loadConfig(configFile);
            }
        } catch (IOException e) {
            VaultRunes.LOGGER.error("Failed to initialize RuneRollConfig", e);
        }
    }

    private static void createDefaultConfig(File configFile) throws IOException {
        JsonObject config = new JsonObject();

        // Default tier weights
        JsonObject tierWeightsJson = new JsonObject();
        tierWeightsJson.addProperty("1", 100);
        tierWeightsJson.addProperty("2", 50);
        tierWeightsJson.addProperty("3", 25);
        tierWeightsJson.addProperty("4", 12);
        tierWeightsJson.addProperty("5", 6);
        config.add("tier_weights", tierWeightsJson);

        // Default god weights
        JsonObject godWeightsJson = new JsonObject();
        godWeightsJson.addProperty("IDONA", 25);
        godWeightsJson.addProperty("VELARA", 25);
        godWeightsJson.addProperty("TENOS", 25);
        godWeightsJson.addProperty("WENDARR", 25);
        config.add("god_weights", godWeightsJson);

        // Default type weights
        JsonObject typeWeightsJson = new JsonObject();
        typeWeightsJson.addProperty("HELMET", 16);
        typeWeightsJson.addProperty("CHESTPLATE", 16);
        typeWeightsJson.addProperty("LEGGINGS", 16);
        typeWeightsJson.addProperty("BOOTS", 16);
        typeWeightsJson.addProperty("SWORD", 18);
        typeWeightsJson.addProperty("AXE", 18);
        config.add("type_weights", typeWeightsJson);

        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(config, writer);
        }

        // Load the default values into memory
        for (int i = 1; i <= 5; i++) {
            tierWeights.put(i, tierWeightsJson.get(String.valueOf(i)).getAsInt());
        }

        for (VaultRune.RuneGod god : VaultRune.RuneGod.values()) {
            godWeights.put(god, godWeightsJson.get(god.name()).getAsInt());
        }

        for (VaultRune.RuneType type : VaultRune.RuneType.values()) {
            typeWeights.put(type, typeWeightsJson.get(type.name()).getAsInt());
        }
    }

    private static void loadConfig(File configFile) {
        try (FileReader reader = new FileReader(configFile)) {
            JsonObject config = JsonParser.parseReader(reader).getAsJsonObject();

            // Load tier weights
            JsonObject tierWeightsJson = config.getAsJsonObject("tier_weights");
            for (int i = 1; i <= 5; i++) {
                String key = String.valueOf(i);
                if (tierWeightsJson.has(key)) {
                    tierWeights.put(i, tierWeightsJson.get(key).getAsInt());
                }
            }

            // Load god weights
            JsonObject godWeightsJson = config.getAsJsonObject("god_weights");
            for (VaultRune.RuneGod god : VaultRune.RuneGod.values()) {
                if (godWeightsJson.has(god.name())) {
                    godWeights.put(god, godWeightsJson.get(god.name()).getAsInt());
                }
            }

            // Load type weights
            JsonObject typeWeightsJson = config.getAsJsonObject("type_weights");
            for (VaultRune.RuneType type : VaultRune.RuneType.values()) {
                if (typeWeightsJson.has(type.name())) {
                    typeWeights.put(type, typeWeightsJson.get(type.name()).getAsInt());
                }
            }
        } catch (IOException e) {
            VaultRunes.LOGGER.error("Failed to load RuneRollConfig", e);
        }
    }

    public static int getTierWeight(int tier) {
        return tierWeights.getOrDefault(tier, 0);
    }

    public static int getGodWeight(VaultRune.RuneGod god) {
        return godWeights.getOrDefault(god, 0);
    }

    public static int getTypeWeight(VaultRune.RuneType type) {
        return typeWeights.getOrDefault(type, 0);
    }

    public static Map<Integer, Integer> getAllTierWeights() {
        return new HashMap<>(tierWeights);
    }

    public static Map<VaultRune.RuneGod, Integer> getAllGodWeights() {
        return new HashMap<>(godWeights);
    }

    public static Map<VaultRune.RuneType, Integer> getAllTypeWeights() {
        return new HashMap<>(typeWeights);
    }
}