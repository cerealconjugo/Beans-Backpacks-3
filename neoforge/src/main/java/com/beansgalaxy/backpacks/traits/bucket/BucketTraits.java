package com.beansgalaxy.backpacks.traits.bucket;

import com.beansgalaxy.backpacks.NeoForgeMain;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class BucketTraits extends GenericTraits {
      public static final String NAME = "bucket";
      private final int size;

      public BucketTraits(@Nullable ResourceLocation location, ModSound sound, int size) {
            super(location, sound);
            this.size = size;
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public BucketClient client() {
            return BucketClient.INSTANCE;
      }

      @Override
      public BucketEntity entity() {
            return BucketEntity.INSTANCE;
      }

      @Override
      public BucketTraits toReference(ResourceLocation location) {
            return new BucketTraits(location, sound(), size);
      }

      public int size() {
            return size;
      }

      @Override
      public Fraction fullness(PatchedComponentHolder holder) {
            FluidStack stack = holder.get(NeoForgeMain.DATA_FLUID);
            if (stack == null || stack.isEmpty())
                  return Fraction.ZERO;

            return Fraction.getFraction(stack.getAmount(), size());
      }

      @Override
      public boolean isEmpty(PatchedComponentHolder holder) {
            return !holder.has(NeoForgeMain.DATA_FLUID);
      }

      @Override
      public int getAnalogOutput(PatchedComponentHolder holder) {
            if (size() > 15) {
                  Fraction fullness = fullness(holder);
                  Fraction fraction = fullness.multiplyBy(Fraction.getFraction(15, 1));
                  return fraction.intValue();
            }

            FluidStack fluid = holder.get(NeoForgeMain.DATA_FLUID);
            if (fluid == null)
                  return 0;

            return fluid.getAmount() / FluidType.BUCKET_VOLUME;
      }

      @Override
      public void stackedOnMe(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {
            BucketMutable mutable = mutable(backpack);
            if (EquipableComponent.canEquip(backpack, slot)) {
                  ItemStack itemStack = mutable.addItem(other, player);

                  if (itemStack == null)
                        itemStack = mutable.removeItem(other, player);

                  if (itemStack == other)
                        return;

                  access.set(itemStack);
            } else if (ClickAction.SECONDARY.equals(click)) {
                  ItemStack itemStack = mutable.addItem(other, player);
                  if (itemStack == null)
                        itemStack = mutable.removeItem(other, player);

                  if (itemStack == other)
                        return;

                  access.set(itemStack);
            }

            mutable.push();
      }

      @Override
      public void stackedOnOther(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir) {
            if (ClickAction.SECONDARY.equals(click) && EquipableComponent.get(backpack).isEmpty()) {
                  BucketMutable mutable = mutable(backpack);
                  ItemStack itemStack = null;
                  if (slot.mayPickup(player))
                        itemStack = mutable.addItem(other, player);

                  if (itemStack == null) {
                        itemStack = mutable.removeItem(other, player);
                        if (!slot.mayPlace(itemStack))
                              return;
                  }

                  if (itemStack == other)
                        return;

                  slot.set(itemStack);
                  mutable.push();
            }
      }

      public BucketMutable mutable(PatchedComponentHolder holder) {
            return new BucketMutable(this, holder);
      }

      @Override
      public TraitComponentKind<? extends GenericTraits> kind() {
            return Traits.BUCKET;
      }
}
