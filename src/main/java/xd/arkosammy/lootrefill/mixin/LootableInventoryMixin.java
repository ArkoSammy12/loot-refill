package xd.arkosammy.lootrefill.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
import xd.arkosammy.lootrefill.util.ducks.LootableContainerBlockEntityAccessor;

@Mixin(LootableInventory.class)
public interface LootableInventoryMixin {

    @Shadow @Nullable Identifier getLootTableId();

    @Shadow @Nullable World getWorld();

    @ModifyReturnValue(method = "readLootTable", at = @At("RETURN"))
    private boolean readCustomDataToNbt(boolean original, @Local(argsOnly = true) NbtCompound nbt){
        if(original && ((Object) this instanceof LootableContainerBlockEntity lootableContainerBlockEntity)) {
            ((LootableContainerBlockEntityAccessor) lootableContainerBlockEntity).lootrefill$readDataFromNbt(nbt);
        }
        return original;
    }

    @ModifyReturnValue(method = "writeLootTable", at = @At("RETURN"))
    private boolean writeCustomDataToNbt(boolean original, @Local(argsOnly = true) NbtCompound nbt) {
        if(original && ((Object) this instanceof LootableContainerBlockEntity lootableContainerBlockEntity)) {
            ((LootableContainerBlockEntityAccessor) lootableContainerBlockEntity).lootrefill$writeDataToNbt(nbt);
        }
        return original;
    }

    @ModifyExpressionValue(method = "generateLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/LootableInventory;getLootTableId()Lnet/minecraft/util/Identifier;"))
    private Identifier modifyLootTableId(Identifier original, @Local(argsOnly = true) @Nullable PlayerEntity player){
        return this.shouldRefillContainer(player) ? original : null;
    }

    // Preserve loot table id when generating loot
    @WrapOperation(method = "generateLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/LootableInventory;setLootTableId(Lnet/minecraft/util/Identifier;)V"))
    private void keepLootTableIdIfNeeded(LootableInventory instance, @Nullable Identifier identifier, Operation<Void> original){
        if(!(this instanceof LootableContainerBlockEntity) || identifier != null) {
            original.call(instance, identifier);
        }
    }

    @Unique
    private boolean shouldRefillContainer(PlayerEntity player){
        World world = this.getWorld();
        if(this.getLootTableId() == null || world == null) {
            return false;
        }
        if(this instanceof LootableContainerBlockEntity lootableContainerBlockEntity) {
            return ((LootableContainerBlockEntityAccessor) lootableContainerBlockEntity).lootrefill$shouldRefillLoot(world, player);
        }
        return false;
    }

}
