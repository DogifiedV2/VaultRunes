package com.dogv2.vaultrunes.registry;

import com.dogv2.vaultrunes.VaultRunes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, VaultRunes.MOD_ID);


    public static final RegistryObject<SoundEvent> IDENTIFICATION_TICK = SOUNDS.register("identification_tick",
            () -> new SoundEvent(new ResourceLocation(VaultRunes.MOD_ID, "identification_tick")));

    public static final RegistryObject<SoundEvent> IDENTIFICATION_FINISH = SOUNDS.register("identification_finish",
            () -> new SoundEvent(new ResourceLocation(VaultRunes.MOD_ID, "identification_finish")));
}