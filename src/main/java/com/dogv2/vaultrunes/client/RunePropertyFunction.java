package com.dogv2.vaultrunes.client;

import com.dogv2.vaultrunes.VaultRunes;
import com.dogv2.vaultrunes.item.VaultRune;
import com.dogv2.vaultrunes.registry.ModItems;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import javax.annotation.Nullable;

public class RunePropertyFunction {

    @OnlyIn(Dist.CLIENT)
    public static void registerItemProperties(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Property for god
            registerRuneProperty(VaultRunes.MOD_ID, "god",
                    (stack, level, entity, seed) -> {
                        VaultRune.RuneGod runeGod = VaultRune.getRuneGod(stack);
                        return runeGod.getGodValue();
                    });

            // Property for tier
            registerRuneProperty(VaultRunes.MOD_ID, "tier",
                    (stack, level, entity, seed) -> {
                        int tier = VaultRune.getRuneTier(stack);
                        return tier / 5.0f;
                    });

            // Property for type
            registerRuneProperty(VaultRunes.MOD_ID, "type",
                    (stack, level, entity, seed) -> {
                        VaultRune.RuneType type = VaultRune.getRuneType(stack);
                        return type.getTypeValue();
                    });

            registerRuneProperty(VaultRunes.MOD_ID, "state",
                    (stack, level, entity, seed) -> {
                        VaultRune.RuneState state = VaultRune.getRuneState(stack);
                        return (float)state.ordinal() / 2.0f;
                    });

            // Property for roll animation
            registerRuneProperty(VaultRunes.MOD_ID, "roll_animation",
                    new ClampedItemPropertyFunction() {
                        @Override
                        public float unclampedCall(ItemStack stack, @Nullable ClientLevel level,
                                                   @Nullable LivingEntity entity, int seed) {
                            if (VaultRune.getRuneState(stack) != VaultRune.RuneState.ROLLING) {
                                return 0;
                            }

                            if (level == null) {
                                return 0;
                            }

                            // Create a smooth cycling animation for the rolling state
                            float time = level.getGameTime() % 60;
                            return time / 60.0f;
                        }
                    });
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static void registerRuneProperty(String modid, String name, ClampedItemPropertyFunction propertyGetter) {
        ResourceLocation id = new ResourceLocation(modid, name);
        ItemProperties.register(ModItems.VAULT_RUNE.get(), id, propertyGetter);
    }
}