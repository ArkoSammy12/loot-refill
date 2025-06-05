package io.github.arkosammy12.lootrefill.mixin;

import io.github.arkosammy12.lootrefill.utils.ViewableContainer;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ViewerCountManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockEntityMixin implements ViewableContainer {

    @Shadow @Final private ViewerCountManager stateManager;

    @Override
    public boolean lootrefill$isBeingViewed() {
        return this.stateManager.getViewerCount() > 0;
    }

}
