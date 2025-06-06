package io.github.arkosammy12.lootrefill.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.arkosammy12.lootrefill.LootRefill;
import io.github.arkosammy12.lootrefill.ducks.LootableContainerBlockEntityDuck;
import io.github.arkosammy12.lootrefill.utils.ConfigUtils;
import io.github.arkosammy12.monkeyconfig.managers.ConfigManagerUtils;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LootableInventory.class)
public interface LootableInventoryMixin extends Inventory {

    // Update the maxRefills data for this container every time it is interacted with
    @WrapMethod(method = "generateLoot")
    private void onTryGenerateLoot(PlayerEntity player, Operation<Void> original) {
        if (!(this instanceof LootableContainerBlockEntity lootableContainerBlockEntity)) {
            original.call(player);
            return;
        }
        long newMaxRefills = ConfigManagerUtils.getRawNumberSettingValue(LootRefill.CONFIG_MANAGER, ConfigUtils.MAX_REFILLS).longValue();
        ((LootableContainerBlockEntityDuck) lootableContainerBlockEntity).lootrefill$setMaxRefills(newMaxRefills);
        original.call(player);
    }

    @WrapOperation(method = "generateLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/LootableInventory;getLootTable()Lnet/minecraft/registry/RegistryKey;"))
    private @Nullable RegistryKey<LootTable> modifyLootTableReturnValue(LootableInventory instance, Operation<RegistryKey<LootTable>> original) {
        RegistryKey<LootTable> originalLootTableKey = original.call(instance);
        World world = instance.getWorld();
        if (world == null || world.isClient()) {
            return originalLootTableKey;
        }
        if (!(instance instanceof LootableContainerBlockEntity lootableContainerBlockEntity)) {
            return originalLootTableKey;
        }
        if (originalLootTableKey != null) {
            ((LootableContainerBlockEntityDuck) lootableContainerBlockEntity).lootrefill$setSavedLootTableKey(originalLootTableKey);
            return originalLootTableKey;
        }
        return !((LootableContainerBlockEntityDuck) lootableContainerBlockEntity).lootrefill$shouldRefillLoot(world) ? null : ((LootableContainerBlockEntityDuck) lootableContainerBlockEntity).lootrefill$getSavedLootTableKey();
    }


    @WrapOperation(method = "generateLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/LootTable;supplyInventory(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/loot/context/LootWorldContext;J)V"))
    private void onLootTableSupplied(LootTable instance, Inventory inventory, LootWorldContext parameters, long seed, Operation<Void> original) {
        if (!(inventory instanceof LootableContainerBlockEntity lootableContainerBlockEntity)) {
            original.call(instance, inventory, parameters, seed);
            return;
        }
        World world = lootableContainerBlockEntity.getWorld();
        if (world == null || world.isClient()) {
            original.call(instance, inventory, parameters, seed);
            return;
        }
        if (seed == 0) {
            ((LootableContainerBlockEntityDuck) lootableContainerBlockEntity).lootrefill$setSavedLootTableSeed(world.getRandom().nextLong());
        }
        ((LootableContainerBlockEntityDuck) lootableContainerBlockEntity).lootrefill$onLootRefilled(world);
        // Call original after resetting the looted flag of the container to prevent infinite recursion due to nested calls to LootableInventory#generateLoot
        original.call(instance, inventory, parameters, ((LootableContainerBlockEntityDuck) lootableContainerBlockEntity).lootrefill$getSavedLootTableSeed());
    }

}
