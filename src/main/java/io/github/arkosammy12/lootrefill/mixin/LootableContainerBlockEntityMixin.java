package io.github.arkosammy12.lootrefill.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.arkosammy12.lootrefill.LootRefill;
import io.github.arkosammy12.lootrefill.ducks.LootableContainerBlockEntityDuck;
import io.github.arkosammy12.lootrefill.utils.ConfigUtils;
import io.github.arkosammy12.lootrefill.utils.LootableContainerCustomData;
import io.github.arkosammy12.lootrefill.utils.Utils;
import io.github.arkosammy12.lootrefill.utils.ViewableContainer;
import io.github.arkosammy12.monkeyconfig.managers.ConfigManagerUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LootableContainerBlockEntity.class)
public abstract class LootableContainerBlockEntityMixin extends LockableContainerBlockEntity implements LootableInventory, LootableContainerBlockEntityDuck {

    @Shadow public abstract boolean isEmpty();

    protected LootableContainerBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public boolean lootrefill$shouldRefillLoot(World world, ServerPlayerEntity player) {
        if (this.lootrefill$getCustomData().getSavedLootTableId() == null) {
            return false;
        }

        // Allow the container to be filled if it is the first time being filled
        long globalRefillCount = this.lootrefill$getCustomData().getGlobalRefillCount();
        if (globalRefillCount <= 0) {
            return true;
        }

        boolean isLooted = this.lootrefill$isLooted();
        if (!isLooted) {
            return false;
        }

        boolean isEmpty = this.isEmptyNoSideEffects();
        boolean refillOnlyWhenEmpty = ConfigManagerUtils.getRawBooleanSettingValue(LootRefill.CONFIG_MANAGER, ConfigUtils.REFILL_ONLY_WHEN_EMPTY);
        if (!isEmpty && refillOnlyWhenEmpty) {
            return false;
        }

        // maxRefills == -1 means unlimited refills. maxRefills == 0 means no refills.
        long globalMaxRefillAmount = this.lootrefill$getCustomData().getGlobalMaxRefillAmount();
        long individualMaxRefillAmount = this.lootrefill$getCustomData().getIndividualRefillAmount();
        long maxRefills = individualMaxRefillAmount >= 0 ? individualMaxRefillAmount : globalMaxRefillAmount;
        if (maxRefills == 0) {
            return false;
        }

        boolean perPlayerRefillCounts = ConfigManagerUtils.getRawBooleanSettingValue(LootRefill.CONFIG_MANAGER, ConfigUtils.PER_PLAYER_REFILL_COUNTS);
        if (perPlayerRefillCounts && maxRefills > 0 && this.lootrefill$getCustomData().getRefillCountForPlayer(player) >= maxRefills) {
            return false;
        }
        if (maxRefills > 0 && globalRefillCount >= maxRefills) {
            return false;
        }

        // Do not refill if it is being viewed
        if (this instanceof ViewableContainer viewableContainer && viewableContainer.lootrefill$isBeingViewed()) {
            return false;
        }

        long lastRefilledTime = this.lootrefill$getCustomData().getLastRefilledTime();
        long currentTime = world.getTime();
        long timeDifference = currentTime - lastRefilledTime;
        long ticksUntilRefill = Utils.secondsToTicks(ConfigManagerUtils.getRawNumberSettingValue(LootRefill.CONFIG_MANAGER, ConfigUtils.TIME_UNTIL_REFILLS).longValue());
        return timeDifference >= ticksUntilRefill;
    }

    @Override
    public void lootrefill$onLootRefilled(World world, ServerPlayerEntity player) {

        long oldRefillCount = this.lootrefill$getCustomData().getGlobalRefillCount();
        long newRefillCount = oldRefillCount + 1;
        long newLastRefilledTime = world.getTime();
        long newMaxRefills = ConfigManagerUtils.getRawNumberSettingValue(LootRefill.CONFIG_MANAGER, ConfigUtils.MAX_REFILLS).longValue();

        this.lootrefill$getCustomData().setLastRefilledTime(newLastRefilledTime);
        this.lootrefill$getCustomData().setGlobalMaxRefillAmount(newMaxRefills);
        this.lootrefill$getCustomData().setGlobalRefillCount(newRefillCount);
        this.lootrefill$getCustomData().setLooted(false);
        this.lootrefill$getCustomData().incrementRefillCountForPlayer(player);
    }

    @Override
    public boolean lootrefill$shouldBeProtected(World world) {
        long refillCount = this.lootrefill$getCustomData().getGlobalRefillCount();
        long maxRefills = this.lootrefill$getCustomData().getGlobalMaxRefillAmount();
        boolean protectLootContainers = ConfigManagerUtils.getRawBooleanSettingValue(LootRefill.CONFIG_MANAGER, ConfigUtils.PROTECT_LOOT_CONTAINERS);
        return protectLootContainers && (this.lootrefill$getCustomData().getSavedLootTableId() != null && (refillCount < maxRefills || maxRefills == -1));
    }

    @Override
    public LootableContainerCustomData lootrefill$getCustomData() {
        return this.getAttachedOrCreate(LootableContainerCustomData.ATTACHMENT);
    }

    @Override
    public boolean lootrefill$isLooted() {
        return this.lootrefill$getCustomData().isLooted() || this.isEmptyNoSideEffects();
    }

    // Check if the container is empty. Do not use the Minecraft provided method since that calls generateLoot(), which we don't want
    @Unique
    private boolean isEmptyNoSideEffects() {
        for (ItemStack stack : this.getHeldStacks()) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @WrapMethod(method = "removeStack(I)Lnet/minecraft/item/ItemStack;")
    private ItemStack onStackRemoved(int slot, Operation<ItemStack> original) {
        this.lootrefill$getCustomData().setLooted(true);
        return original.call(slot);
    }

    @WrapMethod(method = "removeStack(II)Lnet/minecraft/item/ItemStack;")
    private ItemStack onStackSplit(int slot, int amount, Operation<ItemStack> original) {
        this.lootrefill$getCustomData().setLooted(true);
        return original.call(slot, amount);
    }

    @WrapMethod(method = "setStack")
    private void onStackSet(int slot, ItemStack stack, Operation<Void> original) {
        if (stack.isEmpty()) {
            this.lootrefill$getCustomData().setLooted(true);
        }
        original.call(slot, stack);
    }

}
