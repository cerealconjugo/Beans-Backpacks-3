package com.beansgalaxy.backpacks.traits.chest;

import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.components.EnderTraits;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.network.serverbound.PickBlock;
import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ChestTraits extends ItemStorageTraits {
      public static final String NAME = "chest";
      public final int rows;
      public final int columns;

      public ChestTraits(@Nullable ResourceLocation location, ModSound sound, int rows, int columns) {
            super(location, sound);
            this.rows = rows;
            this.columns = columns;
      }

      public static Optional<ChestTraits> get(ItemStack backpack) {
            ChestTraits chestTraits = backpack.get(Traits.CHEST);

            if (chestTraits != null)
                  return Optional.of(chestTraits);

            EnderTraits enderTraits = backpack.get(Traits.ENDER);
            if (enderTraits == null)
                  return Optional.empty();

            Optional<GenericTraits> optional = enderTraits.getTrait();
            if (optional.isEmpty())
                  return Optional.empty();

            if (optional.get() instanceof ChestTraits trait)
                  return Optional.of(trait);

            return Optional.empty();
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public ChestClient client() {
            return ChestClient.INSTANCE;
      }

      @Override
      public ChestTraits toReference(ResourceLocation location) {
            return new ChestTraits(location, sound(), rows, columns);
      }

      public int size() {
            return columns * rows;
      }

      @Override
      public Fraction fullness(PatchedComponentHolder holder) {
            ItemContainerContents contents = holder.get(ITraitData.CHEST);
            if (contents == null)
                  return Fraction.ZERO;

            int fullSlots = contents.nonEmptyStream().mapToInt(stack -> stack.isEmpty() ? 0 : 1).sum();
            return Fraction.getFraction(fullSlots, size());
      }

      @Override
      public boolean isFull(PatchedComponentHolder holder) {
            ItemContainerContents contents = holder.get(ITraitData.CHEST);
            if (contents == null)
                  return false;

            NonNullList<ItemStack> pList = NonNullList.of(ItemStack.EMPTY);
            contents.copyInto(pList);
            return pList.size() == size() && pList.stream().allMatch(stack -> stack.getCount() == stack.getMaxStackSize());
      }

      @Override
      public void stackedOnMe(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {
            boolean menuKeyDown = BackData.get(player).isMenuKeyDown();
            if (menuKeyDown) {
                  if (player.level().isClientSide)
                        client().openTinyMenu(this, slot);

                  cir.setReturnValue(true);
            }
      }

      @Override
      public void stackedOnOther(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir) {

      }

      @Override
      public boolean isEmpty(PatchedComponentHolder holder) {
            return !holder.has(ITraitData.CHEST);
      }

      @Override
      public ChestMutable newMutable(PatchedComponentHolder holder) {
            return new ChestMutable(this, holder);
      }

      public void tinyMenuClick(Slot slot, int index, int button, SlotAccess carriedAccess) {
            ItemStack carried = carriedAccess.get();
            ChestMutable mutable = newMutable(PatchedComponentHolder.of(slot.getItem()));
            ItemStack stack = mutable.getItem(index);

            if (stack.isEmpty() && carried.isEmpty())
                  return;

            if (!stack.isEmpty() && !carried.isEmpty()) {
                  if (ItemStack.isSameItemSameComponents(stack, carried)) {
                        int count = button == 1
                                    ? 1
                                    : carried.getCount();

                        int toAdd = Math.min(stack.getMaxStackSize() - stack.getCount(), count);
                        stack.grow(toAdd);
                        carried.shrink(toAdd);
                  }
                  else {
                        mutable.setItem(index, carried);
                        carriedAccess.set(stack);
                  }
            }
            else if (button == 1) {
                  if (stack.isEmpty()) {
                        ItemStack copy = carried.copyWithCount(1);
                        carried.shrink(1);
                        mutable.setItem(index, copy);
                  }
                  else {
                        int count = Mth.ceil((float) stack.getCount() / 2);
                        ItemStack split = stack.split(count);
                        carriedAccess.set(split);
                  }
            }
            else {
                  mutable.setItem(index, carried);
                  carriedAccess.set(stack);
            }

            mutable.push();
      }

      @Override
      public void hotkeyUse(Slot slot, EquipmentSlot selectedEquipment, int button, ClickType actionType, Player player, CallbackInfo ci) {
            if (selectedEquipment == null) {
                  PatchedComponentHolder holder = PatchedComponentHolder.of(slot.getItem());
                  ChestMutable mutable = newMutable(holder);
                  if (mutable.isEmpty()) {
                        ci.cancel();
                        return;
                  }

                  Inventory inventory = player.getInventory();
                  if (ClickType.PICKUP_ALL.equals(actionType)) {
                        ItemStack carried = player.containerMenu.getCarried();
                        List<ItemStack> stacks = mutable.getItemStacks();
                        ItemStack selectedStack = stacks.getFirst();
                        if (!ItemStack.isSameItemSameComponents(carried, selectedStack) || !canItemFit(holder, carried)) {
                              ci.cancel();
                              return;
                        }

                        for (int i = stacks.size() - 1; i >= 0  && !mutable.isFull(); i--) {
                              ItemStack stack = stacks.get(i);
                              boolean same = ItemStack.isSameItemSameComponents(carried, stack);
                              if (same) {
                                    int stackableSlot = inventory.getSlotWithRemainingSpace(stack);
                                    if (stackableSlot == -1) {
                                          stackableSlot = inventory.getFreeSlot();
                                    }
                                    if (stackableSlot == -1)
                                          continue;

                                    ItemStack removed = mutable.removeItem(i);
                                    if (inventory.add(-1, removed)) {
                                          ci.cancel();
                                    }
                              }
                        }

                        boolean cancelled = ci.isCancelled();
                        if (cancelled) {
                              sound().atClient(player, ModSound.Type.REMOVE);
                              mutable.push();
                        }
                        return;
                  }

                  ItemStack stack = mutable.removeItem(0);
                  int stackableSlot = inventory.getSlotWithRemainingSpace(stack);
                  if (stackableSlot == -1) {
                        stackableSlot = inventory.getFreeSlot();
                  }
                  if (stackableSlot != -1 && inventory.add(-1, stack)) {
                        sound().atClient(player, ModSound.Type.REMOVE);
                        mutable.push();
                        ci.cancel();
                  }
            } else {
                  ItemStack backpack = player.getItemBySlot(selectedEquipment);
                  PatchedComponentHolder holder = PatchedComponentHolder.of(backpack);
                  ChestMutable mutable = newMutable(holder);
                  if (mutable.isFull())
                        return;

                  if (ClickType.PICKUP_ALL.equals(actionType)) {
                        List<ItemStack> stacks = mutable.getItemStacks();
                        ItemStack carried = player.containerMenu.getCarried();
                        if (stacks.isEmpty()
                        || !ItemStack.isSameItemSameComponents(carried, stacks.getFirst())
                        || !canItemFit(holder, carried)
                        ) {
                              ci.cancel();
                              return;
                        }

                        Inventory inventory = player.getInventory();
                        NonNullList<ItemStack> items = inventory.items;
                        for (int i = items.size() - 1; i >= 0  && !mutable.isFull(); i--) {
                              ItemStack stack = items.get(i);
                              if (ItemStack.isSameItemSameComponents(carried, stack)) {
                                    int toAdd = mutable.getMaxAmountToAdd(stack.copy());
                                    int count = Math.min(stack.getMaxStackSize(), toAdd);
                                    ItemStack removed = stack.copyWithCount(count);
                                    stack.shrink(count);
                                    if (mutable.addItem(removed, player) != null) {
                                          ci.cancel();
                                    }
                              }
                        }

                        boolean cancelled = ci.isCancelled();
                        if (cancelled) {
                              sound().atClient(player, ModSound.Type.INSERT);
                              mutable.push();
                        }
                        return;
                  }

                  if (canItemFit(holder, slot.getItem())) {
                        ItemStack slotItem = slot.getItem().copy();
                        int toAdd = mutable.getMaxAmountToAdd(slotItem);
                        ItemStack removed = slot.remove(toAdd);
                        if (mutable.addItem(removed, player) != null) {
                              sound().atClient(player, ModSound.Type.INSERT);
                              mutable.push();
                              ci.cancel();
                        }
                  }
            }
      }

      @Override
      public void hotkeyThrow(Slot slot, PatchedComponentHolder backpack, int button, Player player, boolean menuKeyDown, CallbackInfo ci) {
            if (isEmpty(backpack))
                  return;

            ChestMutable mutable = newMutable(backpack);

            ItemStack removed;
            if (menuKeyDown)
                  removed = mutable.removeItem(0);
            else if (EquipableComponent.get(backpack).isPresent())
            {
                  ItemStack itemStack = mutable.getItemStacks().getFirst();
                  removed = itemStack.getCount() == 1 ? mutable.removeItem(0) : itemStack.split(1);
            }
            else return;

            player.drop(removed, true);
            sound().atClient(player, ModSound.Type.REMOVE);
            mutable.push();
            ci.cancel();
      }

      @Override
      public boolean pickupToBackpack(Player player, EquipmentSlot equipmentSlot, Inventory inventory, ItemStack backpack, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            if (!isFull(backpack)) {
                  inventory.items.forEach(stacks -> {
                        if (ItemStack.isSameItemSameComponents(stacks, stack)) {
                              int present = stacks.getCount();
                              int inserted = stack.getCount();
                              int count = present + inserted;
                              int remainder = Math.max(0, count - stack.getMaxStackSize());
                              count -= remainder;

                              stacks.setCount(count);
                              stack.setCount(remainder);
                        }
                  });

                  if (stack.isEmpty()) {
                        cir.setReturnValue(true);
                        return true;
                  }

                  ChestMutable mutable = newMutable(PatchedComponentHolder.of(backpack));
                  Iterator<ItemStack> iterator = mutable.getItemStacks().iterator();
                  while (iterator.hasNext() && !stack.isEmpty()) {
                        ItemStack itemStack = iterator.next();
                        if (ItemStack.isSameItemSameComponents(itemStack, stack)) {
                              ItemStack returnStack = mutable.addItem(stack, player);
                              if (returnStack != null) {
                                    cir.setReturnValue(true);
                              }
                        }
                  }

                  if (cir.isCancelled() && cir.getReturnValue()) {
                        sound().toClient(player, ModSound.Type.INSERT, 1, 1);
                        mutable.push();

                        if (player instanceof ServerPlayer serverPlayer) {
                              List<Pair<EquipmentSlot, ItemStack>> pSlots = List.of(Pair.of(equipmentSlot, backpack));
                              ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(serverPlayer.getId(), pSlots);
                              serverPlayer.serverLevel().getChunkSource().broadcastAndSend(serverPlayer, packet);
                        }
                  }

                  return stack.isEmpty();
            }
            return false;
      }

      @Override
      public void clientPickBlock(EquipmentSlot equipmentSlot, boolean instantBuild, Inventory inventory, ItemStack itemStack, Player player, CallbackInfo ci) {
            if (instantBuild || inventory.getFreeSlot() == -1)
                  return;

            int slot = inventory.findSlotMatchingItem(itemStack);
            if (slot > -1 || player == null)
                  return;

            ItemStack backpack = player.getItemBySlot(equipmentSlot);
            ItemContainerContents contents = backpack.get(ITraitData.CHEST);
            if (contents == null) {
                  return;
            }

            NonNullList<ItemStack> stacks = NonNullList.of(ItemStack.EMPTY);
            contents.copyInto(stacks);

            for (int j = 0; j < stacks.size(); j++) {
                  ItemStack backpackStack = stacks.get(j);
                  if (ItemStack.isSameItem(itemStack, backpackStack)) {
                        slot = j;
                  }
            }

            if (slot < 0)
                  return;

            PickBlock.send(slot, equipmentSlot);
            sound().atClient(player, ModSound.Type.REMOVE);
            ci.cancel();
      }

      @Override
      public void breakTrait(ServerPlayer pPlayer, ItemStack backpack) {
            ItemContainerContents contents = backpack.get(ITraitData.CHEST);
            if (contents == null) {
                  return;
            }

            contents.nonEmptyStream().forEachOrdered(stack -> {
                  boolean success = pPlayer.getInventory().add(-1, stack);
                  if (!success || !stack.isEmpty()) {
                        pPlayer.drop(stack, true, true);
                  }
            });
      }

      @Override
      public @Nullable ItemStack getFirst(PatchedComponentHolder backpack) {
            ItemContainerContents contents = backpack.get(ITraitData.CHEST);
            if (contents == null) {
                  return null;
            }

            return contents.copyOne();
      }

      @Override
      public TraitComponentKind<? extends GenericTraits> kind() {
            return Traits.CHEST;
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChestTraits that)) return false;
            if (!super.equals(o)) return false;
            return rows == that.rows && columns == that.columns && Objects.equals(location(), that.location()) && Objects.equals(sound(), that.sound());
      }

      @Override
      public int hashCode() {
            return Objects.hash(location(), sound(), rows, columns);
      }

      @Override
      public String toString() {
            return "ChestTraits{" +
                        "rows=" + rows +
                        ", columns=" + columns +
                        ", sound=" + sound() +
                        location().map(
                                    location -> ", location=" + location + '}')
                                    .orElse("}");
      }
}
