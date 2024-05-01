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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xd.arkosammy.lootrefill.LootRefill;
import xd.arkosammy.lootrefill.util.ducks.LootableContainerBlockEntityAccessor;
import xd.arkosammy.lootrefill.util.ducks.VieweableContainer;

@Mixin(LootableContainerBlockEntity.class)
public abstract class LootableContainerBlockEntityMixin extends LockableContainerBlockEntity implements LootableInventory, LootableContainerBlockEntityAccessor {

    @Shadow public abstract boolean isEmpty();

    // getItemStacks
    @Shadow protected abstract DefaultedList<ItemStack> method_11282();

    @Unique
    private long refillCount;

    @Unique
    private long lastSavedTime;

    @Unique
    private boolean looted = false;

    protected  LootableContainerBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void setLastSavedTime(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState, CallbackInfo ci){
        World world = this.getWorld();
        if(world == null) {
            return;
        }
        this.lastSavedTime = world.getTime();
    }

    @Inject(method = "setStack", at = @At("HEAD"))
    private void onStackQuickMoved(int slot, ItemStack stack, CallbackInfo ci) {
        if(stack.isEmpty()) {
            this.onStackRemoved();
        }
    }

    @Inject(method = "removeStack(I)Lnet/minecraft/item/ItemStack;", at = @At(value = "RETURN"))
    private void setLootedOnStackRemoved(int slot, CallbackInfoReturnable<ItemStack> cir) {
        this.onStackRemoved();
    }


    @Inject(method = "removeStack(II)Lnet/minecraft/item/ItemStack;", at = @At(value = "RETURN"))
    private void setLootedOnStackRemovedWithAmount(int slot, int amount, CallbackInfoReturnable<ItemStack> cir) {
        this.onStackRemoved();
    }

    @Unique
    private void onStackRemoved() {
         World world = this.getWorld();
        if(world == null) {
            return;
        }
        boolean refillWhenEmpty = world.getGameRules().getBoolean(LootRefill.REFILL_ONLY_WHEN_EMPTY);

        LootRefill.LOGGER.info("Set as looted: {}", !refillWhenEmpty || this.isEmptyNoSideEffects());
        // Consider this container as looted if refillWhenEmpty is false or if the container is empty
        this.looted = !refillWhenEmpty || this.isEmptyNoSideEffects();
        this.updateLastSavedTime(world);
    }

    @Override
    public void lootrefill$writeDataToNbt(NbtCompound nbt) {
        nbt.putLong("refillCount", this.refillCount);
        nbt.putLong("lastSavedTime", this.lastSavedTime);
        nbt.putBoolean("looted", this.looted);
    }

    @Override
    public void lootrefill$readDataFromNbt(NbtCompound nbt) {
        if(nbt.contains("refillCount")) {
            this.refillCount = nbt.getLong("refillCount");
        }
        if(nbt.contains("lastSavedTime")) {
            this.lastSavedTime = nbt.getLong("lastSavedTime");
        }
        if(nbt.contains("looted")) {
            this.looted = nbt.getBoolean("looted");
        }
    }

    @Override
    public boolean lootrefill$shouldRefillLoot(World world, PlayerEntity player) {
        this.updateLastSavedTime(world);
        boolean isEmpty = this.isEmptyNoSideEffects();
        //this.looted = isEmpty || !world.getGameRules().getBoolean(LootRefill.REFILL_ONLY_WHEN_EMPTY);
        if(!this.looted) {
            return false;
        }
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
        boolean enoughTimePassed = world.getTime() - this.lastSavedTime >= LootRefill.secondsToTicks(world.getGameRules().getInt(LootRefill.SECONDS_UNTIL_REFILL));
        if(!enoughTimePassed) {
            return false;
        }
        return true;
    }

    @Override
    public void lootrefill$onLootRefilled(World world) {
        this.lastSavedTime = world.getTime();
        this.refillCount++;
        this.looted = false;
    }

    @Unique
    private void updateLastSavedTime(World world) {
        if(!this.looted) {
            this.lastSavedTime = world.getTime();
        }
    }

    @Unique
    private boolean isEmptyNoSideEffects() {
        return this.method_11282().stream().allMatch(ItemStack::isEmpty);
    }

}
