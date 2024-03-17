package xd.arkosammy.lootrefill.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xd.arkosammy.lootrefill.LootRefill;
import xd.arkosammy.lootrefill.util.ducks.LootableContainerBlockEntityAccessor;
import xd.arkosammy.lootrefill.util.ducks.VieweableContainer;

// TODO: Remove exports
@Debug(export = true)
@Mixin(LootableContainerBlockEntity.class)
public abstract class LootableContainerBlockEntityMixin extends LockableContainerBlockEntity implements LootableInventory, LootableContainerBlockEntityAccessor {

    @Shadow public abstract boolean isEmpty();

    // getItemStacks
    @Shadow protected abstract DefaultedList<ItemStack> method_11282();

    @Unique
    private long lastRefilledTime;

    @Unique
    private long refillCount;

    @Unique
    private boolean looted = false;

    protected  LootableContainerBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "removeStack(I)Lnet/minecraft/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/LootableContainerBlockEntity;generateLoot(Lnet/minecraft/entity/player/PlayerEntity;)V"))
    private void setLootedOnStackRemoved(int slot, CallbackInfoReturnable<ItemStack> cir) {
        this.looted = true;
    }

    @Inject(method = "removeStack(II)Lnet/minecraft/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/LootableContainerBlockEntity;generateLoot(Lnet/minecraft/entity/player/PlayerEntity;)V"))
    private void setLootedOnStackRemoved(int slot, int amount, CallbackInfoReturnable<ItemStack> cir) {
        this.looted = true;
    }

    @Override
    public void lootrefill$writeDataToNbt(NbtCompound nbt) {
        nbt.putLong("lastRefilledTime", this.lastRefilledTime);
        nbt.putLong("refillCount", this.refillCount);
        nbt.putBoolean("looted", this.looted);
    }

    @Override
    public void lootrefill$readDataFromNbt(NbtCompound nbt) {
        if(nbt.contains("lastRefilledTime")) {
            this.lastRefilledTime = nbt.getLong("lastRefilledTime");
        }
        if(nbt.contains("refillCount")) {
            this.refillCount = nbt.getLong("refillCount");
        }
        if(nbt.contains("looted")) {
            this.looted = nbt.getBoolean("looted");
        }
    }

    @Override
    public boolean lootrefill$shouldRefillLoot(World world, PlayerEntity player) {
        if(!this.looted) {
            return false;
        }
        boolean isEmpty = this.method_11282().stream().allMatch(ItemStack::isEmpty);
        if(!isEmpty && world.getGameRules().getBoolean(LootRefill.REFILL_ONLY_WHEN_EMPTY)) {
            return false;
        }
        // A value of -1 for the "maximumAmountOfLootRefills" gamerule means unlimited refills
        long maxRefills = world.getGameRules().getInt(LootRefill.MAX_REFILLS);
        if (maxRefills >= 0 && this.refillCount >= maxRefills) {
            return false;
        }
        if (this instanceof VieweableContainer vieweableContainer && vieweableContainer.lootrefill$isBeingViewed()) {
            return false;
        }
        return LootRefill.ticksToSeconds(world.getTime()) - lastRefilledTime > world.getGameRules().getInt(LootRefill.SECONDS_UNTIL_REFILL);
    }

    @Override
    public void lootrefill$onLootRefilled(World world) {
        this.lastRefilledTime = LootRefill.ticksToSeconds(world.getTime());
        this.refillCount++;
        this.looted = false;
    }

}
