package xd.arkosammy.lootrefill.mixin;

import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xd.arkosammy.lootrefill.util.ducks.VieweableContainer;

@Mixin(ShulkerBoxBlockEntity.class)
public class ShulkerBoxBlockEntityMixin implements VieweableContainer {

    @Shadow private int viewerCount;

    @Override
    public boolean lootrefill$isBeingViewed() {
        return this.viewerCount > 0;
    }
}
