package com.beansgalaxy.backpacks.components;

import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.util.EmptyStack;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StackableComponent {
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
                  ItemStack stack, ItemStack other, Slot slot,
                  ClickAction action, Player player, SlotAccess access,
                  CallbackInfoReturnable<Boolean> cir
      ) {
            boolean stackEmpty = stack.isEmpty();
            boolean otherEmpty = other.isEmpty();
            if (stackEmpty && otherEmpty)
                  return;

            if (!stack.isStackable() && !stackEmpty)
                  return;

            if (!other.isStackable() && !otherEmpty)
                  return;

            if (!stackEmpty && !otherEmpty) {
                  if (!ItemStack.isSameItem(stack, other) || Objects.equals(stack.getComponents(), other.getComponents()))
                        return;
            }

            if (stack.getMaxStackSize() == stack.getCount()
            || other.getMaxStackSize() == other.getCount())
                  return;

            if (!stackEmpty) {
                  StackableComponent onto = stack.get(ITraitData.STACKABLE);
                  if (onto != null) {
                        SlotAccess slotAccess = SlotAccess.of(slot::getItem, slot::set);
                        Mutable mute = onto.mute(slotAccess);
                        stackOnto(mute, other, action, access, player, cir);
                        return;
                  }
            }

            StackableComponent otherComponent = other.get(ITraitData.STACKABLE);
            if (otherComponent != null) {
                  Mutable mute = otherComponent.mute(access);
                  if (stackEmpty) {
                        if (ClickAction.SECONDARY.equals(action)) {
                              ItemStack itemStack = mute.removeSelected(player);
                              slot.set(itemStack);
                              mute.push(cir);
                        }
                        return;
                  }

                  mute.addItem(stack);
                  mute.push(cir);

                  access.set(ItemStack.EMPTY);
                  slot.setByPlayer(other);
                  return;
            }

            if (!stackEmpty && !otherEmpty) {
                  EmptyStack emptyStack = new EmptyStack(stack.getCount(), stack.getComponentsPatch());
                  EmptyStack emptyOther;

                  if (ClickAction.SECONDARY.equals(action)) {
                        emptyOther = new EmptyStack(1, other.getComponentsPatch());
                        other.shrink(1);
                  }
                  else {
                        emptyOther = new EmptyStack(other.getCount(), other.getComponentsPatch());
                        other.setCount(0);
                  }

                  StackableComponent component = new StackableComponent(List.of(emptyStack, emptyOther));
                  stack.set(ITraitData.STACKABLE, component);
                  stack.setCount(1);
                  other.setCount(0);
                  cir.setReturnValue(true);
            }
      }

      private static void stackOnto(Mutable mute, ItemStack other, ClickAction action, SlotAccess access, Player player, CallbackInfoReturnable<Boolean> cir) {
            if (other.isEmpty()) {
                  if (ClickAction.SECONDARY.equals(action)) {
                        ItemStack itemStack = mute.removeSelected(player);
                        access.set(itemStack);
                        mute.push(cir);
                  }
                  return;
            }

            StackableComponent that = other.get(ITraitData.STACKABLE);
            if (that != null) {
                  Mutable thatMute = that.mute(access);
                  if (ClickAction.SECONDARY.equals(action)) {
                        EmptyStack remove = mute.remove(player);
                        thatMute.findAndAdd(remove);
                  }
                  else mute.mergeWith(thatMute);

                  mute.push(cir);
                  thatMute.push();
                  return;
            }

            mute.addItem(other);
            mute.push(cir);
      }

      public List<EmptyStack> stacks() {
            return stacks;
      }

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

      private Mutable mute(SlotAccess access) {
            return new Mutable(this, access);
      }

      private static class Mutable {
            private final List<EmptyStack> stacks;
            private final SlotSelection selection;
            private final SlotAccess access;
            private final Holder<Item> item;

            public Mutable(StackableComponent component, SlotAccess access) {
                  this(new ArrayList<>(component.stacks), component.selection, access);
            }

            public Mutable(ArrayList<EmptyStack> stacks, SlotSelection selection, SlotAccess access) {
                  this.stacks = stacks;
                  this.selection = selection;
                  this.access = access;
                  this.item = access.get().getItemHolder();
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

            public Fraction getMaxSize() {
                  return EmptyStack.maxSize(stacks, item);
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
                  Fraction maxSize = getMaxSize();
                  int count = EmptyStack.count(stacks);
                  Fraction subtract = maxSize.subtract(Fraction.getFraction(count, 1));
                  int remaining = subtract.intValue();
                  return remaining;
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

}
