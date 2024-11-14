package com.beansgalaxy.backpacks.traits.lunch_box;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.screen.TinyClickType;
import com.beansgalaxy.backpacks.traits.*;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.SlotSelection;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.function.Consumer;

public class LunchBoxTraits extends BundleLikeTraits {
      public static final String NAME = "lunch";

      public LunchBoxTraits(@Nullable ResourceLocation location, ModSound sound, int size) {
            super(location, sound, size, new SlotSelection());
      }

      public LunchBoxTraits(ResourceLocation location, ModSound sound, int size, SlotSelection selection) {
            super(location, sound, size, selection);
      }

      @Override
      public LunchBoxClient client() {
            return LunchBoxClient.INSTANCE;
      }

      @Override
      public LunchBoxEntity entity() {
            return LunchBoxEntity.INSTANCE;
      }

      @Override
      public LunchBoxTraits toReference(ResourceLocation location) {
            return new LunchBoxTraits(location, sound(), size());
      }

      @Override
      public boolean isEmpty(PatchedComponentHolder holder) {
            if (!super.isEmpty(holder))
                  return false;

            List<ItemStack> stacks = holder.get(ITraitData.NON_EDIBLES);
            return stacks == null || stacks.isEmpty();
      }

      @Override
      public String name() {
            return NAME;
      }

      public static void ifPresent(ItemStack lunchBox, Consumer<LunchBoxTraits> ifPresent) {
            LunchBoxTraits boxTraits = lunchBox.get(Traits.LUNCH_BOX);
            if (boxTraits != null) {
                  ifPresent.accept(boxTraits);
                  return;
            }

            ReferenceTrait referenceTrait = lunchBox.get(Traits.REFERENCE);
            if (referenceTrait == null || referenceTrait.isEmpty())
                  return;

            referenceTrait.getTrait().ifPresent(traits -> {
                  if (traits instanceof LunchBoxTraits lunchBoxTraits)
                        ifPresent.accept(lunchBoxTraits);
            });
      }

      public static void firstIsPresent(ItemStack lunchBox, LivingEntity entity, Consumer<ItemStack> ifPresent) {
            ifPresent(lunchBox, traits -> {
                  List<ItemStack> stacks = lunchBox.get(ITraitData.ITEM_STACKS);
                  if (stacks == null || stacks.isEmpty())
                        return;

                  int selectedSlotSafe = entity instanceof Player player
                              ? traits.getSelectedSlotSafe(PatchedComponentHolder.of(lunchBox), player)
                              : 0;

                  ifPresent.accept(stacks.get(selectedSlotSafe));

            });
      }

      public void finishUsingItem(ItemStack backpack, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
            PatchedComponentHolder holder = PatchedComponentHolder.of(backpack);
            LunchBoxMutable mutable = mutable(holder);
            int selectedSlot = entity instanceof Player player
                        ? getSelectedSlotSafe(holder, player)
                        : 0;

            ItemStack stack = mutable.getItemStacks().get(selectedSlot).split(1);
            ItemStack consumedStack = stack.finishUsingItem(level, entity);
            ItemStack itemStack = mutable.addItem(consumedStack, null);
            if (itemStack == null)
                  mutable.addNonEdible(consumedStack);

            mutable.push();
            cir.setReturnValue(backpack);
      }

      @Override
      public void use(Level level, Player player, InteractionHand hand, ItemStack backpack, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
            List<ItemStack> stacks = backpack.get(ITraitData.ITEM_STACKS);
            if (stacks == null || stacks.isEmpty())
                  return;

            int selectedSlotSafe = getSelectedSlotSafe(PatchedComponentHolder.of(backpack), player);
            ItemStack first = stacks.get(selectedSlotSafe);
            FoodProperties $$4 = first.get(DataComponents.FOOD);
            if ($$4 != null) {
                  if (player.canEat($$4.canAlwaysEat())) {
                        player.startUsingItem(hand);
                        cir.setReturnValue(InteractionResultHolder.success(backpack));
                  }
            }
      }

      @Override
      public void tinyMenuClick(PatchedComponentHolder holder, int index, TinyClickType clickType, SlotAccess carriedAccess, Player player) {
            super.tinyMenuClick(holder, index, clickType, carriedAccess, player);
      }

      @Override
      public boolean canItemFit(PatchedComponentHolder holder, ItemStack inserted) {
            return inserted.has(DataComponents.FOOD) && super.canItemFit(holder, inserted);
      }

      @Override
      public LunchBoxMutable mutable(PatchedComponentHolder holder) {
            return new LunchBoxMutable(this, holder);
      }

      @Override
      public String toString() {
            return "LunchBoxTraits{" +
                        "size=" + size() +
                        ", sound=" + sound() +
                        location().map(
                                    location -> ", location=" + location + '}')
                                    .orElse("}");
      }

      @Override
      public TraitComponentKind<? extends GenericTraits> kind() {
            return Traits.LUNCH_BOX;
      }
}
