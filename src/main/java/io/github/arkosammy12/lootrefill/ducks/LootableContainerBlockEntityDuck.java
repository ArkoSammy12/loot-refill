package io.github.arkosammy12.lootrefill.ducks;

import io.github.arkosammy12.lootrefill.utils.LootableContainerCustomData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public interface LootableContainerBlockEntityDuck {

    boolean lootrefill$shouldRefillLoot(World world, ServerPlayerEntity player);

    void lootrefill$onLootRefilled(World world, ServerPlayerEntity player);

    boolean lootrefill$shouldBeProtected(World world);

    LootableContainerCustomData lootrefill$getCustomData();

    boolean lootrefill$isLooted();

}
