package com.dogv2.vaultrunes.registry;

import com.dogv2.vaultrunes.VaultRunes;
import com.dogv2.vaultrunes.item.VaultRune;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, VaultRunes.MOD_ID);


    public static final RegistryObject<Item> VAULT_RUNE = ITEMS.register("rune",
            () -> new VaultRune(new Item.Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(1)));
}