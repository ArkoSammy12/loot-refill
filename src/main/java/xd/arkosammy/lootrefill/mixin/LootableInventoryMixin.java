package xd.arkosammy.lootrefill.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xd.arkosammy.lootrefill.util.ducks.LootableContainerBlockEntityAccessor;

@Mixin(LootableInventory.class)
public interface LootableInventoryMixin {

    @Shadow @Nullable Identifier getLootTableId();

    @Shadow @Nullable World getWorld();

    @ModifyReturnValue(method = "readLootTable", at = @At("RETURN"))
    private boolean readCustomDataFromNbt(boolean original, @Local(argsOnly = true) NbtCompound nbt){
        if(this instanceof LootableContainerBlockEntity lootableContainerBlockEntity) {
            ((LootableContainerBlockEntityAccessor) lootableContainerBlockEntity).lootrefill$readDataFromNbt(nbt);
        }
        return original;
    }

    @ModifyReturnValue(method = "writeLootTable", at = @At("RETURN"))
    private boolean writeCustomDataToNbt(boolean original, @Local(argsOnly = true) NbtCompound nbt) {
        if(this instanceof LootableContainerBlockEntity lootableContainerBlockEntity) {
            ((LootableContainerBlockEntityAccessor) lootableContainerBlockEntity).lootrefill$writeDataToNbt(nbt);
        }
        return original;
    }

    @ModifyExpressionValue(method = "generateLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/LootableInventory;getLootTableId()Lnet/minecraft/util/Identifier;"))
    private Identifier changeRefillConditions(Identifier original, @Local(argsOnly = true) @Nullable PlayerEntity player){
        if(!(this instanceof LootableContainerBlockEntity lootableContainerBlockEntity)) {
            return original;
        }
        // If the loot table id is not null, then store it into the separate cachedLootTableId field for the block entity
        Identifier lootTableId = this.getLootTableId();
        if(lootTableId != null) {
            ((LootableContainerBlockEntityAccessor) lootableContainerBlockEntity).lootrefill$setCachedLootTableId(lootTableId);
        }
        return this.shouldRefillContainer(player) ? ((LootableContainerBlockEntityAccessor)lootableContainerBlockEntity).lootrefill$getCachedLootTableId() : null;
    }

    @Inject(method = "generateLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/LootTable;supplyInventory(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/loot/context/LootContextParameterSet;J)V"))
    private void onLootGenerated(PlayerEntity player, CallbackInfo ci) {
        if(this instanceof LootableContainerBlockEntity lootableContainerBlockEntity) {
            ((LootableContainerBlockEntityAccessor)lootableContainerBlockEntity).lootrefill$onLootRefilled(this.getWorld());
        }
    }

    @Unique
    private boolean shouldRefillContainer(PlayerEntity player){
        World world = this.getWorld();
        if(world == null) {
            return false;
        }
        if(this instanceof LootableContainerBlockEntity lootableContainerBlockEntity) {
            return ((LootableContainerBlockEntityAccessor) lootableContainerBlockEntity).lootrefill$shouldRefillLoot(world, player);
        }
        return false;
    }

}
