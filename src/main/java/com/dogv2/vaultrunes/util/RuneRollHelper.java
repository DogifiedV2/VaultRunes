package com.dogv2.vaultrunes.util;

import com.dogv2.vaultrunes.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;
import java.util.function.BiConsumer;

public class RuneRollHelper {

    private static final int ROLL_TIME = 120;
    private static final int ENTRIES_PER_ROLL = 50;

    /**
     * Ticks the identification process of a rune
     */
    public static void tickToll(ItemStack stack, Player player, Consumer<ItemStack> onRollTick, Consumer<ItemStack> onFinish) {
        Level world = player.getLevel();
        CompoundTag rollTag = stack.getOrCreateTagElement("RuneRollHelper");
        int ticks = rollTag.getInt("RollTicks");
        int lastHit = rollTag.getInt("LastHit");
        double displacement = getDisplacement(ticks);

        if (ticks >= ROLL_TIME) {
            onFinish.accept(stack);
            stack.removeTagKey("RuneRollHelper");
            if (ModSounds.IDENTIFICATION_FINISH.isPresent()) {
                world.playSound(null, player.blockPosition(),
                        ModSounds.IDENTIFICATION_FINISH.get(),
                        SoundSource.PLAYERS, 0.3F, 1.0F);
            }
            return;
        }

        if ((int)displacement != lastHit || ticks == 0) {
            onRollTick.accept(stack);
            rollTag.putInt("LastHit", (int)displacement);
            // Play tick sound
            if (ModSounds.IDENTIFICATION_TICK.isPresent()) {
                world.playSound(null, player.blockPosition(),
                        ModSounds.IDENTIFICATION_TICK.get(),
                        SoundSource.PLAYERS, 0.4F, 1.0F);
            }
        }

        rollTag.putInt("RollTicks", ticks + 1);
    }

    private static double getDisplacement(int tick) {

        double progress = tick / (double)ROLL_TIME; // 0.0 to 1.0
        double slowdownFactor = 1.0 - Math.pow(progress, 2.0); // Starts at 1.0, ends near 0
        int totalEntries = 30;

        return (totalEntries * (1.0 - progress)) * slowdownFactor;
    }
}