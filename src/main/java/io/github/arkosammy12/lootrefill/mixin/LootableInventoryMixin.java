package io.github.arkosammy12.lootrefill.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.arkosammy12.lootrefill.ducks.LootableContainerBlockEntityDuck;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LootableInventory.class)
public interface LootableInventoryMixin extends Inventory {

    @Shadow @Nullable World getWorld();

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
        ((LootableContainerBlockEntityDuck) lootableContainerBlockEntity).lootrefill$setSavedLootTableSeed(seed);
        ((LootableContainerBlockEntityDuck) lootableContainerBlockEntity).lootrefill$onLootRefilled(this.getWorld());
        // TODO: What to do with the saved loot table seed
        original.call(instance, inventory, parameters, seed);
    }

}
