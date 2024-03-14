package xd.arkosammy.lootrefill.mixin;

import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ViewerCountManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xd.arkosammy.lootrefill.util.ducks.VieweableContainer;

@Mixin(ChestBlockEntity.class)
public class ChestBlockEntityMixin implements VieweableContainer {
    @Shadow @Final private ViewerCountManager stateManager;

    @Override
    public boolean lootrefill$isBeingViewed() {
        return this.stateManager.getViewerCount() > 0;
    }
}
