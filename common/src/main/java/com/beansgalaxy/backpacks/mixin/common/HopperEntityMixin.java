package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.traits.generic.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.HopperTraitContainer;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Mixin(HopperBlockEntity.class)
public class HopperEntityMixin {

    @Inject(method = "getEntityContainer", at = @At("RETURN"), cancellable = true)
    private static void getContainerAt(Level level, double x, double y, double z, CallbackInfoReturnable<Container> cir) {
        List<Entity> backpacks = level.getEntities((Entity)null, new AABB(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5), in -> in instanceof BackpackEntity);
        if (!backpacks.isEmpty()) {
            int size = backpacks.size();
            int start = level.random.nextInt(size);
            for (int i = start + 1; i != start; i++) {
                if (i == size)
                    i = 0;

                BackpackEntity backpack = (BackpackEntity) backpacks.get(i);
                Optional<GenericTraits> optional = backpack.getTraits();
                if (optional.isEmpty())
                    continue;

                GenericTraits traits = optional.get();
                Container container = traits.entity().createHopperContainer(backpack, traits);
                if (container == null)
                    continue;

                cir.setReturnValue(container);
                return;
            }
        }

    }

    @Inject(method = "getSlots", cancellable = true, at = @At("HEAD"))
    private static void addBackpackSlotsCheck(Container pContainer, Direction pDirection, CallbackInfoReturnable<int[]> cir) {
        if (pContainer instanceof HopperTraitContainer<?> backpackInventory) {
            int containerSize = backpackInventory.getContainerSize();
            if (!backpackInventory.isFull())
                containerSize += 1;

            IntStream range = IntStream.range(0, containerSize + 1);
            cir.setReturnValue(range.toArray());

        }
    }
}
