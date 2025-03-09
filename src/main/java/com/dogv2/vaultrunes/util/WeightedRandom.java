package com.dogv2.vaultrunes.util;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class WeightedRandom {

    /**
     * Gets a random item from the map based on the weights
     * @param weightMap Map with items as keys and their weights as values
     * @param random Random instance to use
     * @return Randomly selected item based on weights
     */
    public static <T> T getRandomItem(Map<T, Integer> weightMap, Random random) {
        // Handle empty map
        if (weightMap.isEmpty()) {
            throw new IllegalArgumentException("Weighted map cannot be empty");
        }

        // Calculate total weight
        int totalWeight = weightMap.values().stream().mapToInt(Integer::intValue).sum();

        // If all weights are 0, pick randomly with equal chance
        if (totalWeight <= 0) {
            int index = random.nextInt(weightMap.size());
            return weightMap.keySet().stream().skip(index).findFirst().orElseThrow();
        }

        // Pick based on weight
        NavigableMap<Integer, T> probabilityMap = new TreeMap<>();
        int cumulative = 0;

        for (Map.Entry<T, Integer> entry : weightMap.entrySet()) {
            cumulative += entry.getValue();
            probabilityMap.put(cumulative, entry.getKey());
        }

        int randomValue = random.nextInt(totalWeight) + 1;
        return probabilityMap.ceilingEntry(randomValue).getValue();
    }
}