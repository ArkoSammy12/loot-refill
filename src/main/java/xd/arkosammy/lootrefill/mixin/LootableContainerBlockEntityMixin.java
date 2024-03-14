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
import xd.arkosammy.lootrefill.LootRefill;
import xd.arkosammy.lootrefill.util.ducks.LootableContainerBlockEntityAccessor;

@Mixin(LootableContainerBlockEntity.class)
public abstract class LootableContainerBlockEntityMixin extends LockableContainerBlockEntity implements LootableInventory, LootableContainerBlockEntityAccessor {

    @Shadow public abstract boolean isEmpty();

    @Shadow protected abstract DefaultedList<ItemStack> method_11282();

    @Unique
    private long lastRefilledTime;

    @Unique
    private long refillCount = 0;

    protected LootableContainerBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public void lootrefill$writeDataToNbt(NbtCompound nbt) {
        nbt.putLong("lastRefilledTime", this.lastRefilledTime);
        nbt.putLong("refillCount", this.refillCount);
    }

    @Override
    public void lootrefill$readDataFromNbt(NbtCompound nbt) {
        if(nbt.contains("lastRefilledTime")) {
            this.lastRefilledTime = nbt.getLong("lastRefilledTime");
        }
        if(nbt.contains("refillCount")) {
            this.refillCount = nbt.getLong("refillCount");
        }
    }

    @Override
    public boolean lootrefill$shouldRefillLoot(World world, PlayerEntity player) {

        boolean isEmpty = this.method_11282().stream().allMatch(ItemStack::isEmpty);
        boolean shouldRefill = System.currentTimeMillis() - lastRefilledTime > world.getGameRules().getInt(LootRefill.TIME_UNTIL_REFILL) * 1000L;
        long maxRefills = world.getGameRules().getInt(LootRefill.MAX_REFILLS);

        if(!isEmpty && world.getGameRules().getBoolean(LootRefill.REFILL_ONLY_WHEN_EMPTY)) {
            shouldRefill = false;
        } else if (maxRefills >= 0 && this.refillCount > maxRefills) {
            shouldRefill = false;
        }

        if(shouldRefill) {
            this.lastRefilledTime = System.currentTimeMillis();
            this.refillCount++;
        }
        return shouldRefill;
    }

}
