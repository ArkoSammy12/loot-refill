package io.github.arkosammy12.lootrefill.ducks;

import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public interface LootableContainerBlockEntityDuck {

    boolean lootrefill$shouldRefillLoot(World world);

    void lootrefill$onLootRefilled(World world);

    boolean lootrefill$shouldBeProtected(World world);

    void lootrefill$setSavedLootTableKey(RegistryKey<LootTable> lootTableRegistryKey);

    RegistryKey<LootTable> lootrefill$getSavedLootTableKey();

    void lootrefill$setRefillCount(long refillCount);

    long lootrefill$getRefillCount();

    void lootrefill$setMaxRefills(long maxRefills);

    long lootrefill$getMaxRefills();

    void lootrefill$setLastSavedTime(long lastSavedTime);

    long lootrefill$getLastSavedTime();

    void lootrefill$setLooted(boolean looted);

    boolean lootrefill$isLooted();

}
