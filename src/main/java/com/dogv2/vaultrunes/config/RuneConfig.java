package com.dogv2.vaultrunes.config;

import com.dogv2.vaultrunes.VaultRunes;
import com.dogv2.vaultrunes.item.VaultRune;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class RuneConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("vault_runes");

    private static Map<VaultRune.RuneType, RuneTypeConfig> runeConfigs = new HashMap<>();

    public static class RuneTypeConfig {
        private int minLevel;
        private Map<Integer, TierConfig> tiers = new HashMap<>();

        public RuneTypeConfig() {
            // Default constructor for GSON
        }

        public int getMinLevel() {
            return minLevel;
        }

        public TierConfig getTierConfig(int tier) {
            return tiers.getOrDefault(tier, new TierConfig());
        }
    }

    public static class TierConfig {
        private int minLevel = 1;
        private int implicits = 0;
        private int prefixes = 0;
        private int suffixes = 0;

        // Chances to get different mod types when creating a higher tier rune
        private double prefixChance = 0.5;
        private double suffixChance = 0.5;

        public TierConfig() {
            // Default constructor for GSON
        }

        public int getMinLevel() {
            return minLevel;
        }

        public int getImplicits() {
            return implicits;
        }

        public int getPrefixes() {
            return prefixes;
        }

        public int getSuffixes() {
            return suffixes;
        }
    }

    public static void init() {
        try {
            Files.createDirectories(CONFIG_DIR);
        } catch (IOException e) {
            VaultRunes.LOGGER.error("Failed to create config directory", e);
        }
    }

    public static void loadAllConfigs() {
        // Create default configs if they don't exist
        for (VaultRune.RuneType type : VaultRune.RuneType.values()) {
            createDefaultConfig(type);
            loadConfig(type);
        }
    }

    private static void createDefaultConfig(VaultRune.RuneType type) {
        File configFile = CONFIG_DIR.resolve(type.getName().toLowerCase() + "_rune.json").toFile();

        if (!configFile.exists()) {
            RuneTypeConfig config = new RuneTypeConfig();
            config.minLevel = 0; // Base level requirement

            // Setup tiers with increasing requirements
            for (int tier = 1; tier <= 6; tier++) {
                TierConfig tierConfig = new TierConfig();
                tierConfig.minLevel = (tier - 1) * 10; // Level requirement increases with tier
                tierConfig.implicits = Math.min(1, tier / 3);
                tierConfig.prefixes = Math.min(3, (tier + 1) / 2);
                tierConfig.suffixes = Math.min(3, tier / 2);

                config.tiers.put(tier, tierConfig);
            }

            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(config, writer);
            } catch (IOException e) {
                VaultRunes.LOGGER.error("Failed to create default config for " + type.getName(), e);
            }
        }
    }

    private static void loadConfig(VaultRune.RuneType type) {
        File configFile = CONFIG_DIR.resolve(type.getName().toLowerCase() + "_rune.json").toFile();

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                RuneTypeConfig config = GSON.fromJson(reader, RuneTypeConfig.class);
                runeConfigs.put(type, config);
                VaultRunes.LOGGER.info("Loaded config for " + type.getName() + " runes");
            } catch (IOException e) {
                VaultRunes.LOGGER.error("Failed to load config for " + type.getName(), e);
                // Fall back to default
                runeConfigs.put(type, new RuneTypeConfig());
            }
        }
    }

    public static RuneTypeConfig getConfig(VaultRune.RuneType type) {
        return runeConfigs.getOrDefault(type, new RuneTypeConfig());
    }

    public static int getMinLevelForRune(VaultRune.RuneType type, int tier) {
        RuneTypeConfig config = getConfig(type);
        return config.getTierConfig(tier).getMinLevel();
    }

    public static int getImplicitsForRune(VaultRune.RuneType type, int tier) {
        RuneTypeConfig config = getConfig(type);
        return config.getTierConfig(tier).getImplicits();
    }

    public static int getPrefixesForRune(VaultRune.RuneType type, int tier) {
        RuneTypeConfig config = getConfig(type);
        return config.getTierConfig(tier).getPrefixes();
    }

    public static int getSuffixesForRune(VaultRune.RuneType type, int tier) {
        RuneTypeConfig config = getConfig(type);
        return config.getTierConfig(tier).getSuffixes();
    }
}