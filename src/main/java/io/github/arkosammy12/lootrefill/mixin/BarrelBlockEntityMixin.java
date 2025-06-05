package io.github.arkosammy12.lootrefill.mixin;

import io.github.arkosammy12.lootrefill.utils.ViewableContainer;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.ViewerCountManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BarrelBlockEntity.class)
public abstract class BarrelBlockEntityMixin implements ViewableContainer {

    @Shadow @Final private ViewerCountManager stateManager;

    @Override
    public boolean lootrefill$isBeingViewed() {
        return this.stateManager.getViewerCount() > 0;
    }

}
