package com.dogv2.vaultrunes.item;

import com.dogv2.vaultrunes.util.RuneRollHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

public interface IdentifiableItem {

    default boolean tryStartIdentification(Player player, ItemStack stack) {
        if (VaultRune.getRuneState(stack) != VaultRune.RuneState.UNIDENTIFIED) {
            return false;
        }

        VaultRune.setRuneState(stack, VaultRune.RuneState.ROLLING);
        return true;
    }

    default void inventoryIdentificationTick(Player player, ItemStack stack) {
        if (VaultRune.getRuneState(stack) == VaultRune.RuneState.ROLLING) {
            // Pass lambda expressions instead of method references
            RuneRollHelper.tickToll(
                    stack,
                    player,
                    (rollStack) -> this.tickRoll(rollStack, player),
                    (finishStack) -> this.tickFinishRoll(finishStack, player)
            );
        }
    }

    default void instantIdentify(@Nullable Player player, ItemStack stack) {
        if (VaultRune.getRuneState(stack) == VaultRune.RuneState.UNIDENTIFIED) {
            tickRoll(stack, player);
            tickFinishRoll(stack, player);
        }
    }

    default boolean canIdentify(Player player, ItemStack stack) {
        return (stack.getCount() == 1);
    }

    void tickRoll(ItemStack stack, @Nullable Player player);

    void tickFinishRoll(ItemStack stack, @Nullable Player player);
}