package io.github.arkosammy12.lootrefill.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.arkosammy12.lootrefill.LootRefill;
import io.github.arkosammy12.lootrefill.ducks.LootableContainerBlockEntityDuck;
import io.github.arkosammy12.lootrefill.utils.ConfigUtils;
import io.github.arkosammy12.lootrefill.utils.Utils;
import io.github.arkosammy12.lootrefill.utils.ViewableContainer;
import io.github.arkosammy12.monkeyconfig.managers.ConfigManagerUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LootableContainerBlockEntity.class)
public abstract class LootableContainerBlockEntityMixin extends LockableContainerBlockEntity implements LootableInventory, LootableContainerBlockEntityDuck {

    protected LootableContainerBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public boolean lootrefill$shouldRefillLoot(World world) {
        if (this.lootrefill$getSavedLootTableKey() == null) {
            return false;
        }

        // Allow the container to be filled if it is the first time being filled
        long refillCount = this.lootrefill$getRefillCount();
        if (refillCount <= 0) {
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
        long maxRefills = this.lootrefill$getMaxRefills();
        if (maxRefills == 0 || (maxRefills > 0 && refillCount >= maxRefills)) {
            return false;
        }

        // Do not refill if it is being viewed
        if (this instanceof ViewableContainer viewableContainer && viewableContainer.lootrefill$isBeingViewed()) {
            return false;
        }
        long lastSavedTime = lootrefill$getLastSavedTime();
        long currentTime = world.getTime();
        long timeDifference = currentTime - lastSavedTime;
        long ticksUntilRefill = Utils.secondsToTicks(Long.valueOf(ConfigManagerUtils.getRawNumberSettingValue(LootRefill.CONFIG_MANAGER, ConfigUtils.TIME_UNTIL_REFILLS)));
        return timeDifference >= ticksUntilRefill;

    }

    @Override
    public void lootrefill$onLootRefilled(World world) {

        long oldRefillCount = this.lootrefill$getRefillCount();

        long newLastSavedTime = world.getTime();
        long newMaxRefills = Long.valueOf(ConfigManagerUtils.getRawNumberSettingValue(LootRefill.CONFIG_MANAGER, ConfigUtils.MAX_REFILLS));
        long newRefillCount = oldRefillCount + 1;

        this.lootrefill$setLastSavedTime(newLastSavedTime);
        this.lootrefill$setMaxRefills(newMaxRefills);
        this.lootrefill$setRefillCount(newRefillCount);
        this.lootrefill$setLooted(false);
    }

    @Override
    public boolean lootrefill$shouldBeProtected(World world) {
        long refillCount = this.lootrefill$getRefillCount();
        long maxRefills = this.lootrefill$getMaxRefills();
        boolean protectLootContainers = ConfigManagerUtils.getRawBooleanSettingValue(LootRefill.CONFIG_MANAGER, ConfigUtils.PROTECT_LOOT_CONTAINERS);
        return protectLootContainers && (this.lootrefill$getSavedLootTableKey() != null && (refillCount < maxRefills || maxRefills == -1));
    }

    @Override
    public void lootrefill$setSavedLootTableKey(RegistryKey<LootTable> lootTableRegistryKey) {
        this.setAttached(LootRefill.SAVED_LOOT_TABLE_KEY, lootTableRegistryKey);
    }

    @Override
    public RegistryKey<LootTable> lootrefill$getSavedLootTableKey() {
        return this.getAttached(LootRefill.SAVED_LOOT_TABLE_KEY);
    }

    @Override
    public void lootrefill$setSavedLootTableSeed(long lootTableSeed) {
        this.setAttached(LootRefill.SAVED_LOOT_TABLE_SEED, lootTableSeed);
    }

    @Override
    public long lootrefill$getSavedLootTableSeed() {
        return this.getAttachedOrElse(LootRefill.SAVED_LOOT_TABLE_SEED, 0L);
    }

    @Override
    public void lootrefill$setRefillCount(long refillCount) {
        this.setAttached(LootRefill.REFILL_COUNT, refillCount);
    }

    @Override
    public long lootrefill$getRefillCount() {
        return this.getAttachedOrElse(LootRefill.REFILL_COUNT, 0L);
    }


    @Override
    public void lootrefill$setMaxRefills(long maxRefills) {
        this.setAttached(LootRefill.MAX_REFILLS, maxRefills);
    }


    @Override
    public long lootrefill$getMaxRefills() {
        return this.getAttachedOrElse(LootRefill.MAX_REFILLS, -1L);
    }

    @Override
    public void lootrefill$setLastSavedTime(long lastSavedTime) {
        this.setAttached(LootRefill.LAST_SAVED_TIME, lastSavedTime);
    }

    @Override
    public long lootrefill$getLastSavedTime() {
        return this.getAttached(LootRefill.LAST_SAVED_TIME);
    }

    @Override
    public void lootrefill$setLooted(boolean looted) {
        this.setAttached(LootRefill.LOOTED, looted);
    }

    @Override
    public boolean lootrefill$isLooted() {
        return this.getAttached(LootRefill.LOOTED) || this.isEmptyNoSideEffects();
    }

    // Check if the container is empty. Do not use the Minecraft provided method since that calls generateLoot(), which we don't want
    @Unique
    private boolean isEmptyNoSideEffects() {
        return this.getHeldStacks().stream().allMatch(ItemStack::isEmpty);
    }

    @WrapMethod(method = "removeStack(I)Lnet/minecraft/item/ItemStack;")
    private ItemStack onStackRemoved(int slot, Operation<ItemStack> original) {
        this.lootrefill$setLooted(true);
        return original.call(slot);
    }

    @WrapMethod(method = "removeStack(II)Lnet/minecraft/item/ItemStack;")
    private ItemStack onStackSplit(int slot, int amount, Operation<ItemStack> original) {
        this.lootrefill$setLooted(true);
        return original.call(slot, amount);
    }

}
