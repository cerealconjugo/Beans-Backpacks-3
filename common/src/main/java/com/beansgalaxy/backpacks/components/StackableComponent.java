package com.beansgalaxy.backpacks.components;

import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.util.DraggingContainer;
import com.beansgalaxy.backpacks.util.DraggingTrait;
import com.beansgalaxy.backpacks.util.EmptyStack;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StackableComponent implements DraggingTrait {
      private final List<EmptyStack> stacks;
      public final SlotSelection selection;
      public final int count;

      public StackableComponent(List<EmptyStack> stacks) {
            this(stacks, new SlotSelection());
      }

      public StackableComponent(List<EmptyStack> stacks, SlotSelection selection) {
            this(stacks, selection, EmptyStack.count(stacks));
      }

      public StackableComponent(List<EmptyStack> stacks, SlotSelection selection, int count) {
            this.stacks = stacks;
            this.selection = selection;
            this.count = count;
      }

      public static void stackItems(
                  ItemStack stack, Slot slot,
                  ItemStack other, SlotAccess access,
                  ClickAction action, Player player,
                  CallbackInfoReturnable<Boolean> cir
      ) {
            if (stack.isEmpty()) {
                  if (other.isEmpty() || ClickAction.PRIMARY.equals(action))
                        return;

                  SlotAccess slotAccess = SlotAccess.of(slot::getItem, slot::set);
                  splitStackable(other, access, slotAccess, player, cir);
                  return;
            }

            // STACK IS NEVER EMPTY

            if (other.isEmpty()) {
                  if (ClickAction.PRIMARY.equals(action))
                        return;

                  SlotAccess slotAccess = SlotAccess.of(slot::getItem, slot::set);
                  splitStackable(stack, slotAccess, access, player, cir);
                  return;
            }

            // BOTH STACKS ARE NEVER EMPTY

            int stackMaxSize = stack.getMaxStackSize();
            int otherMaxSize = other.getMaxStackSize();

            if (stackMaxSize != otherMaxSize)
                  return;

            if (stack.getCount() == stackMaxSize || other.getCount() == otherMaxSize)
                  return;

            if (!ItemStack.isSameItem(stack, other))
                  return;

            if (stack.getComponentsPatch().equals(other.getComponentsPatch()))
                  return;

            // BOTH STACKS HAVE MATCHING ITEMS
            // BOTH STACKS HAVE ACCEPTABLE COUNTS
            // BOTH STACKS HAVE MATCHING MAX STACK SIZES

            StackableComponent stackComp = stack.get(ITraitData.STACKABLE);
            StackableComponent otherComp = other.get(ITraitData.STACKABLE);

            if (stackComp != null && otherComp != null) {
                  SlotAccess slotAccess = SlotAccess.of(slot::getItem, slot::set);
                  Mutable stackMute = stackComp.mute(slotAccess);
                  Mutable otherMute = otherComp.mute(access);

                  if (ClickAction.SECONDARY.equals(action)) {
                        int otherSelection = otherMute.selection.getSelectedSlot(player);
                        EmptyStack selectedStack = otherMute.stacks.get(otherSelection);
                        int add = stackMute.toAdd(selectedStack);
                        if (add < 1)
                              return;

                        EmptyStack emptyStack = selectedStack.copyWithCount(1);
                        selectedStack.amount -= 1;

                        stackMute.findAndAdd(emptyStack);
                  }
                  else {
                        int maxSize = stackMute.maxStackSize;
                        int count = stackComp.count;
                        for (EmptyStack emptyStack : otherMute.stacks) {
                              int toAdd = Math.min(maxSize - count, emptyStack.amount);
                              EmptyStack newEmptyStack = emptyStack.copyWithCount(toAdd);
                              emptyStack.amount -= toAdd;

                              stackMute.findAndAdd(newEmptyStack);

                              count += toAdd;
                              if (count == maxSize)
                                    break;
                        }
                  }

                  stackMute.push(cir);
                  otherMute.push();
                  return;
            }

            if (stackComp == null && otherComp == null) {
                  EmptyStack emptyStack = new EmptyStack(stack.getCount(), stack.getComponentsPatch());
                  EmptyStack emptyOther;

                  if (ClickAction.SECONDARY.equals(action)) {
                        emptyOther = new EmptyStack(1, other.getComponentsPatch());
                        other.shrink(1);
                  }
                  else {
                        int toAdd = Math.min(stackMaxSize - stack.getCount(), other.getCount());
                        emptyOther = new EmptyStack(toAdd, other.getComponentsPatch());
                        other.shrink(toAdd);
                  }

                  StackableComponent component = new StackableComponent(List.of(emptyStack, emptyOther));
                  stack.set(ITraitData.STACKABLE, component);
                  stack.setCount(1);
                  cir.setReturnValue(true);
                  return;
            }

            // ONLY ONE STACK HAS STACKING

            if (otherComp != null) {
                  ArrayList<EmptyStack> list = new ArrayList<>();
                  EmptyStack emptyStack = new EmptyStack(stack.getCount(), stack.getComponentsPatch());
                  list.add(emptyStack);

                  if (ClickAction.SECONDARY.equals(action)) {
                        Mutable mute = otherComp.mute(access);
                        int selectedSlot = mute.selection.getSelectedSlot(player);
                        EmptyStack selectedStack = mute.stacks.get(selectedSlot);

                        int min = Math.min(stackMaxSize - stack.getCount(), other.getCount());
                        if (min < 1)
                              return;

                        boolean equals = Objects.equals(emptyStack.data(), selectedStack.data());
                        if (equals) {
                              stack.grow(1);
                              selectedStack.amount -= min;
                              mute.push(cir);
                              return;
                        }

                        EmptyStack emptyOther = new EmptyStack(min, selectedStack.data());
                        list.add(emptyOther);
                        selectedStack.amount -= min;
                        mute.push();
                  }
                  else {
                        Mutable mute = otherComp.mute(access);

                        int totalCount = stack.getCount();
                        for (EmptyStack otherEmpty : mute.stacks) {
                              int toAdd = Math.min(stackMaxSize - totalCount, otherEmpty.amount);
                              otherEmpty.amount -= toAdd;

                              if (emptyStack.amount != stackMaxSize && Objects.equals(otherEmpty.data(), emptyStack.data())) {
                                    emptyStack.amount += toAdd;
                              }
                              else {
                                    EmptyStack copy = otherEmpty.copyWithCount(toAdd);
                                    list.add(copy);
                              }

                              totalCount += toAdd;
                              if (totalCount == stackMaxSize)
                                    break;
                        }

                        mute.push();
                  }

                  List<EmptyStack> stacks = list.stream().toList();
                  StackableComponent component = new StackableComponent(stacks);
                  stack.set(ITraitData.STACKABLE, component);
                  stack.setCount(1);

                  cir.setReturnValue(true);
                  return;
            }

            SlotAccess slotAccess = SlotAccess.of(slot::getItem, slot::set);
            Mutable mute = stackComp.mute(slotAccess);
            int add = mute.toAdd(EmptyStack.of(other));
            if (add < 1)
                  return;

            if (ClickAction.SECONDARY.equals(action)) {
                  EmptyStack emptyStack = new EmptyStack(1, other.getComponentsPatch());
                  mute.findAndAdd(emptyStack);
                  other.shrink(1);
            }
            else {
                  EmptyStack emptyStack = new EmptyStack(add, other.getComponentsPatch());
                  mute.findAndAdd(emptyStack);
                  other.shrink(add);
            }

            mute.push(cir);
      }

      private static void splitStackable(ItemStack stack, SlotAccess slot, SlotAccess access, Player player, CallbackInfoReturnable<Boolean> cir) {
            StackableComponent component = stack.get(ITraitData.STACKABLE);
            if (component == null)
                  return;

            Mutable mute = component.mute(slot);
            ItemStack itemStack = mute.removeSelected(player);
            access.set(itemStack);
            mute.push(cir);
      }

      private static boolean itemsDoNotStack(ItemStack stack, ItemStack other) {
            boolean stackEmpty = stack.isEmpty();
            boolean otherEmpty = other.isEmpty();

            if (stackEmpty && otherEmpty)
                  return true;

            if (!stack.isStackable() && !stackEmpty)
                  return true;

            if (!other.isStackable() && !otherEmpty)
                  return true;

            if (!stackEmpty && !otherEmpty) {
                  if (!ItemStack.isSameItem(stack, other) || Objects.equals(stack.getComponents(), other.getComponents()))
                        return true;
            }

            int stackMaxSize = stack.getMaxStackSize();
            int otherMaxSize = other.getMaxStackSize();

            if (stackMaxSize != otherMaxSize)
                  return true;

            if (stackMaxSize == stack.getCount()
            || otherMaxSize == other.getCount())
                  return true;

            return false;
      }

      public static boolean stackItems(Inventory inventory, int slot, ItemStack stack, ItemStack other) {
            if (itemsDoNotStack(stack, other))
                  return false;

            boolean stackEmpty = stack.isEmpty();
            if (!stackEmpty) {
                  StackableComponent onto = stack.get(ITraitData.STACKABLE);
                  if (onto != null) {
                        SlotAccess slotAccess = SlotAccess.of(
                                    () -> inventory.getItem(slot),
                                    set -> inventory.setItem(slot, set)
                        );

                        Mutable mute = onto.mute(slotAccess);
                        int remaining = mute.getRemaining();
                        if (remaining < 1)
                              return false;

                        if (other.isEmpty())
                              return false;

                        StackableComponent that = other.get(ITraitData.STACKABLE);
                        if (that != null) {
                              ItemStack[] pseudo = new ItemStack[] {other};
                              SlotAccess access = SlotAccess.of(
                                          () -> pseudo[0],
                                          set -> pseudo[0] = set
                              );

                              Mutable thatMute = that.mute(access);
                              thatMute.mergeWith(mute);
                              mute.push();
                              thatMute.push();

                              if (!pseudo[0].isEmpty() && !inventory.add(-1, pseudo[0]))
                                    inventory.player.drop(pseudo[0], true);
                              return true;
                        }

                        mute.addItem(other);
                        mute.push();
                        return true;
                  }
            }

            StackableComponent otherComponent = other.get(ITraitData.STACKABLE);
            if (otherComponent != null) {
                  if (stackEmpty)
                        return false;

                  inventory.setItem(slot, other.copy());
                  other.setCount(0);

                  SlotAccess slotAccess = SlotAccess.of(
                              () -> inventory.getItem(slot),
                              set -> inventory.setItem(slot, set)
                  );

                  Mutable mute = otherComponent.mute(slotAccess);
                  mute.addItem(stack);
                  mute.push();

                  return true;
            }

            if (!stackEmpty && !other.isEmpty()) {
                  int stackCount = stack.getCount();
                  EmptyStack emptyStack = new EmptyStack(stackCount, stack.getComponentsPatch());

                  int maxStackSize = stack.getMaxStackSize();
                  int min = Math.min(maxStackSize - stackCount, other.getCount());
                  EmptyStack emptyOther = new EmptyStack(min, other.getComponentsPatch());
                  other.shrink(min);

                  StackableComponent component = new StackableComponent(List.of(emptyStack, emptyOther));
                  stack.set(ITraitData.STACKABLE, component);
                  stack.setCount(1);

                  if (!other.isEmpty())
                        return inventory.add(-1, other);
                  else
                        return true;
            }

            return false;
      }

      public List<EmptyStack> stacks() {
            return stacks;
      }

      private static int toAdd(List<EmptyStack> stacks, Holder<Item> holder, EmptyStack stack) {
            int count = EmptyStack.count(stacks);
            return Math.min(stack.getMaxStackSize(holder) - count, stack.amount);
      }

      public void clickSlot(DraggingContainer drag, Player player, PatchedComponentHolder holder) {
            Slot slot = drag.backpackDraggedSlot;

            if (drag.backpackDragType != 0)
                  drag.backpackDragType = 0;

            Holder<Item> item = player.containerMenu.getCarried().getItemHolder();
            if (stacks.isEmpty())
                  return;

            ItemStack itemStack = stacks.getFirst().withItem(item);
            if (!slot.hasItem()) {
                  if (AbstractContainerMenu.canItemQuickReplace(slot, itemStack, true) && slot.mayPlace(itemStack)) {
                        drag.backpackDraggedSlots.put(slot, ItemStack.EMPTY);
                        drag.slotClicked(slot, slot.index, 1, ClickType.PICKUP);
                  }
            }
      }

      private Mutable mute(SlotAccess access) {
            return new Mutable(this, access);
      }

      // ===================================================================================================================== MUTABLE

      private static class Mutable {
            private final List<EmptyStack> stacks;
            private final SlotSelection selection;
            private final SlotAccess access;
            private final Holder<Item> item;
            private final int maxStackSize;

            public Mutable(StackableComponent component, SlotAccess access) {
                  this(new ArrayList<>(component.stacks), component.selection, access);
            }

            public Mutable(ArrayList<EmptyStack> stacks, SlotSelection selection, SlotAccess access) {
                  this.stacks = stacks;
                  this.selection = selection;
                  this.access = access;
                  this.item = access.get().getItemHolder();
                  this.maxStackSize = access.get().getMaxStackSize();
            }

            public void push() {
                  stacks.removeIf(EmptyStack::isEmpty);

                  if (stacks.isEmpty()) {
                        access.get().setCount(0);
                        return;
                  }

                  if (stacks.size() == 1) {
                        EmptyStack emptyStack = stacks.getFirst();
                        ItemStack itemStack = emptyStack.withItem(item);
                        access.set(itemStack);
                        return;
                  }

                  StackableComponent newValue = new StackableComponent(stacks.stream().toList(), selection);
                  access.get().set(ITraitData.STACKABLE, newValue);
            }

            public void addItem(ItemStack other) {
                  int remaining = getRemaining();
                  int min = Math.min(remaining, other.getCount());
                  EmptyStack emptyStack = new EmptyStack(min, other.getComponentsPatch());
                  findAndAdd(emptyStack);
                  other.shrink(min);
            }

            @NotNull
            public ItemStack removeSelected(Player player) {
                  EmptyStack removed = remove(player);
                  return removed.withItem(item);
            }

            public void mergeWith(Mutable that) {
                  int remaining = getRemaining();

                  List<EmptyStack> thatStacks = that.stacks;
                  for (int i = thatStacks.size() - 1; i >= 0; i--) {
                        EmptyStack emptyStack = thatStacks.get(i);
                        int thatCount = emptyStack.getCount();
                        if (remaining > thatCount) {
                              remaining -= thatCount;
                              EmptyStack removed = thatStacks.remove(i);
                              findAndAdd(removed);
                        }
                        else {
                              int remainder = thatCount - remaining;
                              EmptyStack copy = emptyStack.copyWithCount(remainder);
                              emptyStack.amount -= remainder;
                              findAndAdd(copy);
                              break;
                        }
                  }

                  selection.clamp(stacks.size() - 1);
            }

            private void findAndAdd(EmptyStack stack) {
                  for (EmptyStack emptyStack : stacks) {
                        if (Objects.equals(stack.data(), emptyStack.data())) {
                              int count = Math.min(emptyStack.getMaxStackSize(item) - emptyStack.amount, stack.amount);
                              stack.amount -= count;
                              emptyStack.amount += count;
                        }
                        if (stack.isEmpty())
                              return;
                  }

                  this.stacks.add(stack);
            }

            private int getRemaining() {
                  int count = EmptyStack.count(stacks);
                  return maxStackSize - count;
            }

            private int toAdd(EmptyStack stack) {
                  return StackableComponent.toAdd(stacks, item, stack);
            }

            public void push(CallbackInfoReturnable<Boolean> cir) {
                  push();
                  cir.setReturnValue(true);
            }

            public EmptyStack remove(Player player) {
                  int selectedSlot = selection.getSelectedSlot(player);
                  selection.clamp(stacks.size() - 2);
                  return stacks.remove(selectedSlot);
            }
      }

// ===================================================================================================================== CODECS

      public static final Codec<StackableComponent> CODEC = EmptyStack.EMPTY_STACK_CODEC.listOf()
                  .xmap(StackableComponent::new, StackableComponent::stacks);

      public static final StreamCodec<RegistryFriendlyByteBuf, StackableComponent> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public StackableComponent decode(RegistryFriendlyByteBuf buf) {
                  List<EmptyStack> stacks = EmptyStack.LIST_EMPTY_STACK_STREAM_CODEC.decode(buf);
                  SlotSelection selection = SlotSelection.STREAM_CODEC.decode(buf);
                  int count = buf.readInt();
                  return new StackableComponent(stacks, selection, count);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, StackableComponent stackable) {
                  EmptyStack.LIST_EMPTY_STACK_STREAM_CODEC.encode(buf, stackable.stacks);
                  SlotSelection.STREAM_CODEC.encode(buf, stackable.selection);
                  buf.writeInt(stackable.count);
            }
      };

}
