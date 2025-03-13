package com.dogv2.vaultrunes;

import com.dogv2.vaultrunes.client.RunePropertyFunction;
import com.dogv2.vaultrunes.config.RuneConfig;
import com.dogv2.vaultrunes.config.RuneRollConfig;
import com.dogv2.vaultrunes.registry.ModItems;
import com.dogv2.vaultrunes.registry.ModSounds;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(VaultRunes.MOD_ID)
public class VaultRunes {

    public static final String MOD_ID = "vault_runes";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VaultRunes() {
        // Register the setup method for modloading
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::clientSetup);

        ModItems.ITEMS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);

        RuneConfig.init();
        RuneRollConfig.init();

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("VaultRunes mod initialized");

        event.enqueueWork(RuneConfig::loadAllConfigs);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        // Register item property functions for the rune item
        RunePropertyFunction.registerItemProperties(event);
    }
}