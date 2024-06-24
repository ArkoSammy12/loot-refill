package xd.arkosammy.lootrefill.util.ducks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public interface LootableContainerBlockEntityAccessor {

    boolean lootrefill$shouldRefillLoot(World world, PlayerEntity player);

    void lootrefill$writeDataToNbt(NbtCompound nbtCompound);

    void lootrefill$readDataFromNbt(NbtCompound nbtCompound);

    void lootrefill$onLootRefilled(World world);

    void lootrefill$setCachedLootTableKey(RegistryKey<LootTable> lootTableRegistryKey);

    RegistryKey<LootTable> lootrefill$getCachedLootTableId();

    boolean lootrefill$shouldBeProtected(World world);

    void lootrefill$setMaxRefills(long maxRefills);

}
