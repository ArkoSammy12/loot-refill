package io.github.arkosammy12.lootrefill.mixin;

import io.github.arkosammy12.lootrefill.utils.ViewableContainer;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ShulkerBoxBlockEntity.class)
public abstract class ShulkerBoxBlockEntityMixin implements ViewableContainer {

    @Shadow private int viewerCount;

    @Override
    public boolean lootrefill$isBeingViewed() {
        return this.viewerCount > 0;
    }

}
