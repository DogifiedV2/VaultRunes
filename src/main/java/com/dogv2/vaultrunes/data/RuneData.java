package com.dogv2.vaultrunes.data;

import com.dogv2.vaultrunes.config.RuneConfig;
import com.dogv2.vaultrunes.item.VaultRune;
import iskallia.vault.gear.attribute.VaultGearModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles storage and retrieval of rune modifiers data in the item NBT
 */
public class RuneData {
    private static final String TAG_RUNE_DATA = "RuneData";
    private static final String TAG_IMPLICITS = "Implicits";
    private static final String TAG_PREFIXES = "Prefixes";
    private static final String TAG_SUFFIXES = "Suffixes";
    private static final String TAG_MODIFIER_ID = "id";
    private static final String TAG_MODIFIER_TIER = "tier";
    private static final String TAG_MODIFIER_VALUE = "value";

    private final List<RuneModifier> implicits = new ArrayList<>();
    private final List<RuneModifier> prefixes = new ArrayList<>();
    private final List<RuneModifier> suffixes = new ArrayList<>();

    private final VaultRune.RuneType runeType;
    private final int runeTier;

    public RuneData(VaultRune.RuneType runeType, int runeTier) {
        this.runeType = runeType;
        this.runeTier = runeTier;
    }

    /**
     * Get the maximum number of implicit modifiers this rune can have
     */
    public int getMaxImplicits() {
        return RuneConfig.getImplicitsForRune(runeType, runeTier);
    }

    /**
     * Get the maximum number of prefix modifiers this rune can have
     */
    public int getMaxPrefixes() {
        return RuneConfig.getPrefixesForRune(runeType, runeTier);
    }

    /**
     * Get the maximum number of suffix modifiers this rune can have
     */
    public int getMaxSuffixes() {
        return RuneConfig.getSuffixesForRune(runeType, runeTier);
    }

    /**
     * Add an implicit modifier to this rune
     */
    public boolean addImplicit(RuneModifier modifier) {
        if (implicits.size() >= getMaxImplicits()) {
            return false;
        }
        implicits.add(modifier);
        return true;
    }

    /**
     * Add a prefix modifier to this rune
     */
    public boolean addPrefix(RuneModifier modifier) {
        if (prefixes.size() >= getMaxPrefixes()) {
            return false;
        }
        prefixes.add(modifier);
        return true;
    }

    /**
     * Add a suffix modifier to this rune
     */
    public boolean addSuffix(RuneModifier modifier) {
        if (suffixes.size() >= getMaxSuffixes()) {
            return false;
        }
        suffixes.add(modifier);
        return true;
    }

    /**
     * Get all implicits on this rune
     */
    public List<RuneModifier> getImplicits() {
        return new ArrayList<>(implicits);
    }

    /**
     * Get all prefixes on this rune
     */
    public List<RuneModifier> getPrefixes() {
        return new ArrayList<>(prefixes);
    }

    /**
     * Get all suffixes on this rune
     */
    public List<RuneModifier> getSuffixes() {
        return new ArrayList<>(suffixes);
    }

    /**
     * Read rune data from an ItemStack
     */
    public static RuneData read(ItemStack stack) {
        VaultRune.RuneType type = VaultRune.getRuneType(stack);
        int tier = VaultRune.getRuneTier(stack);

        RuneData data = new RuneData(type, tier);

        if (stack.hasTag() && stack.getTag().contains(TAG_RUNE_DATA)) {
            CompoundTag runeData = stack.getTag().getCompound(TAG_RUNE_DATA);

            // Read implicits
            if (runeData.contains(TAG_IMPLICITS)) {
                ListTag implicits = runeData.getList(TAG_IMPLICITS, Tag.TAG_COMPOUND);
                for (int i = 0; i < implicits.size(); i++) {
                    CompoundTag modTag = implicits.getCompound(i);
                    data.implicits.add(RuneModifier.fromTag(modTag));
                }
            }

            // Read prefixes
            if (runeData.contains(TAG_PREFIXES)) {
                ListTag prefixes = runeData.getList(TAG_PREFIXES, Tag.TAG_COMPOUND);
                for (int i = 0; i < prefixes.size(); i++) {
                    CompoundTag modTag = prefixes.getCompound(i);
                    data.prefixes.add(RuneModifier.fromTag(modTag));
                }
            }

            // Read suffixes
            if (runeData.contains(TAG_SUFFIXES)) {
                ListTag suffixes = runeData.getList(TAG_SUFFIXES, Tag.TAG_COMPOUND);
                for (int i = 0; i < suffixes.size(); i++) {
                    CompoundTag modTag = suffixes.getCompound(i);
                    data.suffixes.add(RuneModifier.fromTag(modTag));
                }
            }
        }

        return data;
    }

    /**
     * Write rune data to an ItemStack
     */
    public void write(ItemStack stack) {
        CompoundTag runeData = new CompoundTag();

        // Write implicits
        ListTag implicitsList = new ListTag();
        for (RuneModifier modifier : implicits) {
            implicitsList.add(modifier.toTag());
        }
        runeData.put(TAG_IMPLICITS, implicitsList);

        // Write prefixes
        ListTag prefixesList = new ListTag();
        for (RuneModifier modifier : prefixes) {
            prefixesList.add(modifier.toTag());
        }
        runeData.put(TAG_PREFIXES, prefixesList);

        // Write suffixes
        ListTag suffixesList = new ListTag();
        for (RuneModifier modifier : suffixes) {
            suffixesList.add(modifier.toTag());
        }
        runeData.put(TAG_SUFFIXES, suffixesList);

        stack.getOrCreateTag().put(TAG_RUNE_DATA, runeData);
    }

    /**
     * Represents a single modifier on a rune
     */
    public static class RuneModifier {
        private final String id;
        private final int tier;
        private final double value;

        public RuneModifier(String id, int tier, double value) {
            this.id = id;
            this.tier = tier;
            this.value = value;
        }

        public String getId() {
            return id;
        }

        public int getTier() {
            return tier;
        }

        public double getValue() {
            return value;
        }

        public CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putString(TAG_MODIFIER_ID, id);
            tag.putInt(TAG_MODIFIER_TIER, tier);
            tag.putDouble(TAG_MODIFIER_VALUE, value);
            return tag;
        }

        public static RuneModifier fromTag(CompoundTag tag) {
            String id = tag.getString(TAG_MODIFIER_ID);
            int tier = tag.getInt(TAG_MODIFIER_TIER);
            double value = tag.getDouble(TAG_MODIFIER_VALUE);
            return new RuneModifier(id, tier, value);
        }
    }
}