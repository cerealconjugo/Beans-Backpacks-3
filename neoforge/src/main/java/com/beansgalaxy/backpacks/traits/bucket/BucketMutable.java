package com.beansgalaxy.backpacks.traits.bucket;

import com.beansgalaxy.backpacks.NeoForgeMain;
import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.generic.MutableTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BucketMutable extends FluidTank implements MutableTraits {
      private final BucketTraits traits;
      private final PatchedComponentHolder holder;

      public BucketMutable(BucketTraits traits, PatchedComponentHolder holder) {
            super(traits.size() * FluidType.BUCKET_VOLUME);
            this.traits = traits;
            this.holder = holder;
            FluidStack fluidStack = holder.getOrElse(NeoForgeMain.DATA_FLUID, () -> FluidStack.EMPTY);
            fluid = fluidStack;

      }

      public @Nullable ItemStack addItem(ItemStack stack, Player player) {
            Optional<IFluidHandlerItem> optional = FluidUtil.getFluidHandler(stack);
            if (optional.isEmpty())
                  return null;

            IFluidHandlerItem handler = optional.get();
            FluidStack fluid = holder.get(NeoForgeMain.DATA_FLUID);
            FluidStack drain;
            if (fluid == null || fluid.isEmpty()) {
                  drain = handler.drain(capacity, FluidAction.SIMULATE);
                  if (drain.isEmpty())
                        return null;
            } else {
                  int space = capacity - fluid.getAmount();
                  drain = handler.drain(fluid.copyWithAmount(space), FluidAction.SIMULATE);
            }

            if (!drain.isEmpty()) {
                  fill(drain, FluidAction.EXECUTE);
                  handler.drain(drain, FluidAction.EXECUTE);
                  return handler.getContainer();
            }

            return null;
      }

      public ItemStack removeItem(ItemStack stack, Player player) {
            FluidStack fluid = holder.get(NeoForgeMain.DATA_FLUID);
            if (fluid == null || fluid.isEmpty())
                  return stack;

            Optional<IFluidHandlerItem> optional = FluidUtil.getFluidHandler(stack);
            if (optional.isEmpty())
                  return stack;

            IFluidHandlerItem handler = optional.get();
            int drain = handler.fill(fluid, FluidAction.EXECUTE);
            if (drain > 0)
                  return handler.getContainer();

            return stack;
      }


      @Override
      public void push() {

      }

      @Override
      public ModSound sound() {
            return null;
      }

      @Override
      public Fraction fullness() {
            return null;
      }
}
