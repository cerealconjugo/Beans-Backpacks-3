package com.beansgalaxy.backpacks.traits.generic;

import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.SlotSelection;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

public abstract class BundleLikeTraits extends ItemStorageTraits {
      public final SlotSelection slotSelection;
      private final Fraction fullness;

      public BundleLikeTraits(Fraction fullness) {
            this.fullness = fullness;
            this.slotSelection = new SlotSelection();
      }

      public BundleLikeTraits(Fraction fullness, SlotSelection slotSelection) {
            this.fullness = fullness;
            this.slotSelection = slotSelection;
      }

      @Override
      public Fraction fullness() {
            return fullness;
      }

      @Override
      public boolean isEmpty() {
            return stacks().isEmpty();
      }

      public abstract MutableBundleLike mutable();

      @Override
      public int getSelectedSlot(Player player) {
            return slotSelection.getSelectedSlot(player);
      }

      @Override
      public int getSelectedSlotSafe(Player player) {
            return slotSelection.getSelectedSlotSafe(player);
      }

      @Override
      public void setSelectedSlot(Player player, int selectedSlot) {
            slotSelection.setSelectedSlot(player, selectedSlot);
      }

      @Override
      public void limitSelectedSlot(int slot, int size) {
            slotSelection.limit(slot, size);
      }

      @Override
      public void stackedOnMe(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {
            MutableBundleLike mutable = mutable();
            boolean empty = !EquipableComponent.testIfPresent(backpack, equipable -> !equipable.traitRemovable());
            if (empty) {
                  if (ClickAction.SECONDARY.equals(click)) {
                        if (other.isEmpty()) {
                              if (mutable().getItemStacks().isEmpty())
                                    return;

                              int selectedSlot = getSelectedSlotSafe(player);

                              access.set(mutable.removeItemNoUpdate(selectedSlot));
                              sound().atClient(player, ModSound.Type.REMOVE);

                              int size = mutable.stacks.size();
                              limitSelectedSlot(selectedSlot, size);
                        }
                        else if (mutable.addItem(other, getSelectedSlot(player), player) != null) {
                              sound().atClient(player, ModSound.Type.INSERT);
                        }

                        freezeAndCancel(backpack, cir, mutable);
                  }
            }
            else if (EquipableComponent.canEquip(backpack, slot)) {
                  if (other.isEmpty()) {
                        if (mutable.isEmpty())
                              return;


                        int selectedSlot = getSelectedSlotSafe(player);
                        ItemStack stack = ClickAction.SECONDARY.equals(click)
                                    ? mutable.splitItem(selectedSlot)
                                    : mutable.removeItemNoUpdate(selectedSlot);

                        if (stack != null) {
                              access.set(stack);
                              sound().atClient(player, ModSound.Type.REMOVE);
                              int size = mutable.stacks.size();
                              limitSelectedSlot(selectedSlot, size);
                        }
                  } else {
                        ItemStack returned;
                        int selectedSlot = getSelectedSlot(player);
                        if (ClickAction.PRIMARY.equals(click)) {
                              returned = mutable.addItem(other, selectedSlot, player);
                        } else {
                              returned = mutable.addItem(other.copyWithCount(1), selectedSlot, player);
                              if (returned != null) {
                                    other.shrink(1);
                              }
                        }

                        if (returned == null) {
                              cir.setReturnValue(true);
                              return;
                        } else {
                              sound().atClient(player, ModSound.Type.INSERT);
                        }
                  }

                  freezeAndCancel(backpack, cir, mutable);
            }
      }

      @Override
      public void stackedOnOther(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir) {
            boolean empty = !EquipableComponent.testIfPresent(backpack, equipable ->
                        !equipable.slots().test(EquipmentSlot.OFFHAND)
            );

            boolean equals = ClickAction.SECONDARY.equals(click);
            if (equals && empty) {
                  MutableBundleLike mutable = mutable();
                  ModSound sound = sound();
                  if (other.isEmpty()) {
                        ItemStack stack = mutable.removeItemNoUpdate(other, player);
                        if (stack.isEmpty() || !slot.mayPlace(stack))
                              return;

                        slot.set(stack);
                        sound.atClient(player, ModSound.Type.REMOVE);
                        int size = mutable.stacks.size();
                        limitSelectedSlot(0, size);
                  }
                  else if (slot.mayPickup(player)) {
                        if (mutable.addItem(other, player) != null)
                              sound.atClient(player, ModSound.Type.INSERT);
                  }
                  else return;

                  freezeAndCancel(backpack, cir, mutable);
            }
      }

      @Override
      public boolean canItemFit(ItemStack inserted) {
            return !inserted.isEmpty() && super.canItemFit(inserted);
      }

      public abstract class MutableBundleLike implements MutableItemStorage {
            protected final List<ItemStack> stacks;
            private final int size;

            public MutableBundleLike(BundleLikeTraits traits) {
                  this.stacks = new ArrayList<>(traits.stacks());
                  this.size = traits.size();
            }

            public int size() {
                  return size;
            }

            public List<ItemStack> getItemStacks() {
                  return stacks;
            }

            @Override @Nullable
            public ItemStack addItem(ItemStack stack, @Nullable Player player) {
                  return addItem(stack, 0, player);
            }

            @Nullable
            public ItemStack addItem(ItemStack inserted, int slot, @Nullable Player player) {
                  if (!canItemFit(inserted))
                        return null;

                  int spaceLeft = this.getMaxAmountToAdd(inserted);
                  int toInsert = Math.min(inserted.getCount(), spaceLeft);
                  if (toInsert == 0)
                        return null;

                  int count = toInsert;
                  if (inserted.isStackable()) {
                        for (ItemStack stored : getItemStacks()) {
                              if (inserted.isEmpty() || count < 1)
                                    return ItemStack.EMPTY;

                              if (ItemStack.isSameItemSameComponents(stored, inserted)) {
                                    int insert = Math.min(stored.getMaxStackSize() - stored.getCount(), count);
                                    stored.grow(insert);
                                    inserted.shrink(insert);
                                    count -= insert;
                              }
                        }
                  }

                  if (!inserted.isEmpty()) {
                        int selectedSlot = Math.min(getSelectedSlot(player), getItemStacks().size());
                        ItemStack split = inserted.split(count);
                        getItemStacks().add(selectedSlot, split);
                        slotSelection.grow(selectedSlot);
                  }

                  return inserted;
            }

            @Override
            public ItemStack removeItemNoUpdate(ItemStack carried, Player player) {
                  return removeItemNoUpdate(0);
            }

            @Override @NotNull
            public ItemStack removeItemNoUpdate(int slot) {
                  ItemStack returned = ItemStack.EMPTY;
                  List<ItemStack> stacks = getItemStacks();
                  if (stacks.size() > slot) {
                        ItemStack stack = stacks.get(slot);
                        int maxCount = stack.getMaxStackSize();
                        if (stack.getCount() > maxCount) {
                              stack.shrink(maxCount);
                              returned = stack.copyWithCount(maxCount);
                        } else
                              returned = stacks.remove(slot);
                  }
                  return returned;
            }

            public ItemStack splitItem(int slot) {
                  List<ItemStack> stacks = getItemStacks();
                  ItemStack stack = stacks.get(slot);
                  ItemStack split = stack.split(Mth.ceil(stack.getCount() / 2f));

                  if (stack.isEmpty() && slot < stacks.size())
                        stacks.remove(slot);

                  return split;
            }

            @Override
            public int getMaxAmountToAdd(ItemStack stack) {
                  Fraction size = Fraction.getFraction(size(), 1);
                  Fraction weight = Traits.getWeight(getItemStacks());
                  Fraction weightLeft = size.subtract(weight);
                  return Math.max(weightLeft.divideBy(Traits.getItemWeight(stack)).intValue(), 0);
            }

            public boolean isEmpty() {
                  return this.getItemStacks().isEmpty();
            }

            @Override
            abstract public BundleLikeTraits trait();
      }
}
