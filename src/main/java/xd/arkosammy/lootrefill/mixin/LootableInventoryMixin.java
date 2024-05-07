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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xd.arkosammy.lootrefill.LootRefill;
import xd.arkosammy.lootrefill.util.ducks.LootableContainerBlockEntityAccessor;

@Mixin(LootableInventory.class)
public interface LootableInventoryMixin {

    @Shadow @Nullable Identifier getLootTableId();

    @Shadow @Nullable World getWorld();

    @Inject(method = "readLootTable", at = @At("RETURN"))
    private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfoReturnable<Boolean> cir){
        if(this instanceof LootableContainerBlockEntity lootableContainerBlockEntity) {
            ((LootableContainerBlockEntityAccessor) lootableContainerBlockEntity).lootrefill$readDataFromNbt(nbt);
        }
    }

    @Inject(method = "writeLootTable", at = @At("RETURN"))
    private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfoReturnable<Boolean> cir) {
        if(this instanceof LootableContainerBlockEntity lootableContainerBlockEntity) {
            ((LootableContainerBlockEntityAccessor) lootableContainerBlockEntity).lootrefill$writeDataToNbt(nbt);
        }
    }

    @ModifyExpressionValue(method = "generateLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/LootableInventory;getLootTableId()Lnet/minecraft/util/Identifier;"))
    private Identifier changeRefillConditions(Identifier original, @Local(argsOnly = true) @Nullable PlayerEntity player){
        if(!(this instanceof LootableContainerBlockEntity lootableContainerBlockEntity)) {
            return original;
        }
        World world = this.getWorld();
        if(world != null) {
            ((LootableContainerBlockEntityAccessor) lootableContainerBlockEntity).lootrefill$setMaxRefills(world.getGameRules().getInt(LootRefill.MAX_REFILLS));
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
