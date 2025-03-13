package com.dogv2.vaultrunes.item;

import com.dogv2.vaultrunes.config.RuneRollConfig;
import com.dogv2.vaultrunes.registry.ModItems;
import com.dogv2.vaultrunes.util.WeightedRandom;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class VaultRune extends Item implements IdentifiableItem {
    public static final String TAG_RUNE_TYPE = "RuneType";
    public static final String TAG_RUNE_TIER = "RuneTier";
    public static final String TAG_RUNE_GOD  = "RuneGod";
    public static final String TAG_RUNE_STATE = "RuneState";

    private static final Random RANDOM = new Random();

    // Rune Types
    public enum RuneType {
        HELMET(0, "Helmet"),
        CHESTPLATE(1, "Chestplate"),
        LEGGINGS(2, "Leggings"),
        BOOTS(3, "Boots"),
        SWORD(4, "Sword"),
        AXE(5, "Axe");

        private final int id;
        private final String name;

        RuneType(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }

        public static RuneType fromId(int id) {
            for (RuneType type : values()) {
                if (type.id == id) return type;
            }
            return HELMET; // Default
        }

        public float getTypeValue() {
            return id / (float) (values().length - 1);
        }
    }

    public enum RuneGod {
        IDONA(0, "Idona", ChatFormatting.RED),
        VELARA(1, "Velara", ChatFormatting.GREEN),
        TENOS(2, "Tenos", ChatFormatting.AQUA),
        WENDARR(3, "Wendarr", ChatFormatting.GOLD);

        private final int id;
        private final String name;
        private final ChatFormatting color;

        RuneGod(int id, String name, ChatFormatting color) {
            this.id = id;
            this.name = name;
            this.color = color;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public ChatFormatting getColor() { return color; }

        public static RuneGod fromId(int id) {
            for (RuneGod god : values()) {
                if (god.id == id) return god;
            }
            return IDONA; // Default
        }

        public float getGodValue() {
            return id / 4.0f;
        }
    }

    public enum RuneState {
        UNIDENTIFIED,
        ROLLING,
        IDENTIFIED
    }


    // begin class

    public VaultRune(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }

        if (getRuneState(stack) == RuneState.UNIDENTIFIED && canIdentify(player, stack)) {
            if (tryStartIdentification(player, stack)) {
                return InteractionResultHolder.consume(stack);
            }
        }

        return super.use(level, player, hand);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return getRuneState(stack) == RuneState.ROLLING || super.isFoil(stack);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (!level.isClientSide && entity instanceof Player player && getRuneState(stack) == RuneState.ROLLING) {
            inventoryIdentificationTick(player, stack);
        }
    }

    // Color based on the tier
    public static ChatFormatting getTierFormatting(int tier) {
        return switch(tier) {
            case 2 -> ChatFormatting.AQUA;
            case 3 -> ChatFormatting.YELLOW;
            case 4 -> ChatFormatting.LIGHT_PURPLE;
            case 5 -> ChatFormatting.GREEN;
            case 6 -> ChatFormatting.RED;
            default -> ChatFormatting.GRAY;
        };
    }

    // Override tooltip to show info based on state
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> components, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, components, flag);

        RuneState state = getRuneState(stack);

        if (state == RuneState.UNIDENTIFIED) {
            components.add(new TextComponent("Right-click to identify").withStyle(ChatFormatting.GRAY));
            return;
        }

        if (state == RuneState.ROLLING) {
            components.add(new TextComponent("Identifying...").withStyle(ChatFormatting.GRAY));
            return;
        }

        // If identified, show the regular tooltip info
        int tier = getRuneTier(stack);
        RuneType type = getRuneType(stack);
        RuneGod god = getRuneGod(stack);

        // God line
        MutableComponent godLine = new TextComponent("");
        godLine.append(new TextComponent("God: ").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        godLine.append(new TextComponent(god.getName()).setStyle(Style.EMPTY.withColor(god.getColor())));
        components.add(godLine);

        // Armor piece type
        MutableComponent typeLine = new TextComponent("");
        typeLine.append(new TextComponent("Type: ").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        typeLine.append(new TextComponent(type.getName()).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF))));
        components.add(typeLine);

        // Tier
        MutableComponent tierLine = new TextComponent("");
        tierLine.append(new TextComponent("Tier: ").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        tierLine.append(new TextComponent(getRuneTierString(tier)).setStyle(Style.EMPTY.withColor(getTierFormatting(tier))));
        components.add(tierLine);
    }

    // Item Name based on state
    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        RuneState state = getRuneState(stack);

        if (state == RuneState.UNIDENTIFIED) {
            return new TextComponent("Unidentified Vault Rune").withStyle(ChatFormatting.GRAY);
        } else if (state == RuneState.ROLLING) {
            return new TextComponent("Vault Rune").withStyle(ChatFormatting.GRAY);
        }

        // Regular name for identified runes
        return new TextComponent("Vault Rune");
    }


    public static RuneState getRuneState(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(TAG_RUNE_STATE)) {
            int stateId = stack.getTag().getInt(TAG_RUNE_STATE);
            return RuneState.values()[stateId];
        }
        return RuneState.IDENTIFIED;
    }

    public static void setRuneState(ItemStack stack, RuneState state) {
        stack.getOrCreateTag().putInt(TAG_RUNE_STATE, state.ordinal());
    }

    // get the armor type
    public static RuneType getRuneType(ItemStack stack) {
        if (stack.hasTag()) {
            assert stack.getTag() != null;
            if (stack.getTag().contains(TAG_RUNE_TYPE)) {
                return RuneType.fromId(stack.getTag().getInt(TAG_RUNE_TYPE));
            }
        }
        return RuneType.HELMET; // Default
    }

    // get tier of the rune
    public static int getRuneTier(ItemStack stack) {
        if (stack.hasTag()) {
            assert stack.getTag() != null;
            if (stack.getTag().contains(TAG_RUNE_TIER)) {
                return stack.getTag().getInt(TAG_RUNE_TIER);
            }
        }
        return 1;
    }

    public static String getRuneTierString(int runeTier) {
        return switch (runeTier) {
            case 2 -> "Common";
            case 3 -> "Rare";
            case 4 -> "Epic";
            case 5 -> "Omega";
            default -> "Scrappy";
        };
    }

    // get the god
    public static RuneGod getRuneGod(ItemStack stack) {
        if (stack.hasTag()) {
            assert stack.getTag() != null;
            if (stack.getTag().contains(TAG_RUNE_GOD)) {
                return RuneGod.fromId(stack.getTag().getInt(TAG_RUNE_GOD));
            }
        }
        return RuneGod.IDONA; // Default
    }

    // Set methods
    public static void setRuneType(ItemStack stack, RuneType type) {
        stack.getOrCreateTag().putInt(TAG_RUNE_TYPE, type.getId());
    }

    public static void setRuneTier(ItemStack stack, int tier) {
        if (tier < 1) tier = 1;
        if (tier > 5) tier = 5;
        stack.getOrCreateTag().putInt(TAG_RUNE_TIER, tier);
    }

    public static void setRuneGod(ItemStack stack, RuneGod god) {
        stack.getOrCreateTag().putInt(TAG_RUNE_GOD, god.getId());
    }

    // Method to create a rune
    public static ItemStack createRune(RuneType type, int tier, RuneGod god) {
        ItemStack stack = new ItemStack(ModItems.VAULT_RUNE.get());
        setRuneType(stack, type);
        setRuneTier(stack, tier);
        setRuneGod(stack, god);
        setRuneState(stack, RuneState.IDENTIFIED);
        return stack;
    }

    // Method to create an unidentified rune
    public static ItemStack createUnidentifiedRune() {
        ItemStack stack = new ItemStack(ModItems.VAULT_RUNE.get());
        setRuneState(stack, RuneState.UNIDENTIFIED);
        return stack;
    }

    @Override
    public void fillItemCategory(@NotNull CreativeModeTab tab, @NotNull NonNullList<ItemStack> items) {
        if (!this.allowedIn(tab)) {
            return;
        }

        // Add an unidentified rune to creative menu
        items.add(createUnidentifiedRune());

        // Add all the regular runes
        for (RuneGod god : RuneGod.values()) {
            for (RuneType type : RuneType.values()) {
                for (int tier = 1; tier <= 5; tier++) {
                    items.add(createRune(type, tier, god));
                }
            }
        }
    }

    private boolean allowedIn(CreativeModeTab pCategory) {
        if (this.getCreativeTabs().stream().anyMatch((tab) -> tab == pCategory)) {
            return true;
        } else {
            CreativeModeTab creativemodetab = this.getItemCategory();
            return creativemodetab != null && (pCategory == CreativeModeTab.TAB_SEARCH || pCategory == creativemodetab);
        }
    }

    // IdentifiableItem
    @Override
    public void tickRoll(ItemStack stack, @Nullable Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        long gameTime = player != null ? player.level.getGameTime() : 0;

        int stackSeed = stack.hashCode();

        int cycleIndex = (int) ((gameTime + stackSeed) % 20);

        int godIndex = ((cycleIndex + (stackSeed % 4)) % 4);
        int tierValue = ((cycleIndex % 5) + 1 + (stackSeed % 5)) % 5 + 1;
        int typeIndex = ((cycleIndex + (stackSeed % 6)) % 6);

        tag.putInt(TAG_RUNE_GOD, godIndex);
        tag.putInt(TAG_RUNE_TIER, tierValue);
        tag.putInt(TAG_RUNE_TYPE, typeIndex);
    }

    @Override
    public void tickFinishRoll(ItemStack stack, @Nullable Player player) {
        Map<RuneType, Integer> typeWeights = RuneRollConfig.getAllTypeWeights();
        RuneType selectedType = WeightedRandom.getRandomItem(typeWeights, RANDOM);

        Map<RuneGod, Integer> godWeights = RuneRollConfig.getAllGodWeights();
        RuneGod selectedGod = WeightedRandom.getRandomItem(godWeights, RANDOM);

        Map<Integer, Integer> tierWeights = RuneRollConfig.getAllTierWeights();
        int selectedTier = WeightedRandom.getRandomItem(tierWeights, RANDOM);

        setRuneType(stack, selectedType);
        setRuneGod(stack, selectedGod);
        setRuneTier(stack, selectedTier);
        setRuneState(stack, RuneState.IDENTIFIED);
    }
}