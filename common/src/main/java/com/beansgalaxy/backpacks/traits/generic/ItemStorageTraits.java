package com.beansgalaxy.backpacks.traits.generic;

import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.network.serverbound.PickBlock;
import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.IDeclaredFields;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class ItemStorageTraits implements GenericTraits {

      private static Optional<ItemStorageTraits> get(DataComponentHolder stack) {
            ReferenceTrait referenceTrait = stack.get(Traits.REFERENCE);
            if (referenceTrait != null && !referenceTrait.isEmpty())
                  return referenceTrait.getTrait().map(traits -> {
                        if (traits instanceof ItemStorageTraits storageTraits)
                              return storageTraits;
                        return null;
                  });

            for (TraitComponentKind<? extends ItemStorageTraits, ? extends IDeclaredFields> type : Traits.STORAGE_TRAITS) {
                  ItemStorageTraits traits = stack.get(type);
                  if (traits != null)
                        return Optional.of(traits);
            }

//            EnderTraits enderTraits = stack.get(Traits.ENDER);
//            if (enderTraits != null) {
//                  return enderTraits.getTrait().map(traits -> {
//                        if (traits instanceof ItemStorageTraits storageTraits)
//                              return storageTraits;
//                        else
//                              return null;
//                  });
//            }

            return Optional.empty();
      }

      public static void runIfPresent(ItemStack stack, Consumer<ItemStorageTraits> runnable) {
            if (!stack.isEmpty()) {
                  Optional<ItemStorageTraits> traits = get(stack);
                  traits.ifPresent(runnable);
            }
      }

      public static void runIfPresent(ItemStack stack, Consumer<ItemStorageTraits> runnable, Runnable orElse) {
            if (!stack.isEmpty()) {
                  Optional<ItemStorageTraits> traits = get(stack);
                  traits.ifPresentOrElse(runnable, orElse);
            }
      }

      public static void runIfPresentOrEnder(ItemStack stack, Consumer<ItemStorageTraits> runnable) {
            runIfPresent(stack, runnable, () -> {

            });
      }

      public static void runIfEquipped(Player player, BiPredicate<ItemStorageTraits, EquipmentSlot> runnable) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                  ItemStack stack = player.getItemBySlot(slot);
                  if (stack.isEmpty())
                        continue;

                  Optional<ItemStorageTraits> traits = get(stack);
                  if (traits.isEmpty())
                        continue;

                  if (runnable.test(traits.get(), slot))
                        return;
            }
      }

      public static boolean testIfPresent(ItemStack stack, Predicate<ItemStorageTraits> predicate) {
            return !stack.isEmpty() && get(stack).map(predicate::test).orElse(false);
      }

      public abstract List<ItemStack> stacks();

      public abstract boolean isEmpty();

      public abstract MutableItemStorage mutable();

      public int getSelectedSlot(Player player) {
            return 0;
      }

      public int getSelectedSlotSafe(Player player) {
            return 0;
      }

      public void setSelectedSlot(Player sender, int selectedSlot) {

      }

      public void limitSelectedSlot(int slot, int size) {

      }

      public void hotkeyUse(Slot slot, EquipmentSlot selectedEquipment, int button, ClickType actionType, Player player, CallbackInfo ci) {
            MutableItemStorage mutable = mutable();
            if (selectedEquipment == null) {
                  List<ItemStack> stacks = mutable.getItemStacks();
                  if (stacks.isEmpty()) {
                        ci.cancel();
                        return;
                  }

                  Inventory inventory = player.getInventory();
                  int selectedSlot = getSelectedSlotSafe(player);
                  if (ClickType.PICKUP_ALL.equals(actionType)) {
                        ItemStack carried = player.containerMenu.getCarried();
                        ItemStack selectedStack = stacks.get(selectedSlot);
                        if (!ItemStack.isSameItemSameComponents(carried, selectedStack) || !canItemFit(carried)) {
                              ci.cancel();
                              return;
                        }

                        List<ItemStack> items = mutable.getItemStacks();
                        for (int i = items.size() - 1; i >= 0  && !isFull(); i--) {
                              ItemStack stack = items.get(i);
                              boolean same = ItemStack.isSameItemSameComponents(carried, stack);
                              if (same) {
                                    int stackableSlot = inventory.getSlotWithRemainingSpace(stack);
                                    if (stackableSlot == -1) {
                                          stackableSlot = inventory.getFreeSlot();
                                    }
                                    if (stackableSlot == -1)
                                          continue;

                                    ItemStack removed = mutable.removeItemNoUpdate(i);
                                    if (inventory.add(-1, removed)) {
                                          ci.cancel();
                                    }
                              }
                        }

                        boolean cancelled = ci.isCancelled();
                        if (cancelled) {
                              sound().atClient(player, ModSound.Type.REMOVE);
                              freezeAndCancel(PatchedComponentHolder.of(slot.getItem()), mutable);
                        }
                        return;
                  }

                  ItemStack stack = mutable.removeItemNoUpdate(selectedSlot);
                  int stackableSlot = inventory.getSlotWithRemainingSpace(stack);
                  if (stackableSlot == -1) {
                        stackableSlot = inventory.getFreeSlot();
                  }
                  if (stackableSlot != -1 && inventory.add(-1, stack)) {
                        sound().atClient(player, ModSound.Type.REMOVE);
                        freezeAndCancel(PatchedComponentHolder.of(slot.getItem()), mutable);
                        ci.cancel();
                  }
            } else {
                  if (isFull())
                        return;

                  if (ClickType.PICKUP_ALL.equals(actionType)) {
                        List<ItemStack> stacks = mutable.getItemStacks();
                        ItemStack carried = player.containerMenu.getCarried();
                        if (stacks.isEmpty()
                        || !ItemStack.isSameItemSameComponents(carried, stacks.getFirst())
                        || !canItemFit(carried)
                        ) {
                              ci.cancel();
                              return;
                        }

                        Inventory inventory = player.getInventory();
                        NonNullList<ItemStack> items = inventory.items;
                        for (int i = items.size() - 1; i >= 0  && !isFull(); i--) {
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
                              freezeAndCancel(PatchedComponentHolder.of(player.getItemBySlot(selectedEquipment)), mutable);
                        }
                        return;
                  }

                  if (canItemFit(slot.getItem())) {
                        ItemStack slotItem = slot.getItem().copy();
                        int toAdd = mutable.getMaxAmountToAdd(slotItem);
                        ItemStack removed = slot.remove(toAdd);
                        if (mutable.addItem(removed, player) != null) {
                              sound().atClient(player, ModSound.Type.INSERT);
                              freezeAndCancel(PatchedComponentHolder.of(player.getItemBySlot(selectedEquipment)), mutable);
                              ci.cancel();
                        }
                  }
            }
      }

      public void hotkeyThrow(Slot slot, PatchedComponentHolder backpack, int button, Player player, boolean menuKeyDown, CallbackInfo ci) {
            if (isEmpty())
                  return;

            MutableItemStorage mutable = mutable();
            int selectedSlot = getSelectedSlotSafe(player);

            ItemStack removed;
            if (menuKeyDown)
                  removed = mutable.removeItemNoUpdate(selectedSlot);
            else if (EquipableComponent.get(backpack).isPresent())
            {
                  ItemStack itemStack = mutable.getItemStacks().get(selectedSlot);
                  removed = itemStack.getCount() == 1 ? mutable.removeItemNoUpdate(selectedSlot) : itemStack.split(1);
            }
            else return;

            player.drop(removed, true);
            limitSelectedSlot(selectedSlot, mutable.getItemStacks().size());

            sound().atClient(player, ModSound.Type.REMOVE);
            freezeAndCancel(backpack, mutable);
            ci.cancel();
      }

      public void clientPickBlock(EquipmentSlot equipmentSlot, boolean instantBuild, Inventory inventory, ItemStack itemStack, Player player, CallbackInfo ci) {
            if (instantBuild || inventory.getFreeSlot() == -1)
                  return;

            int slot = inventory.findSlotMatchingItem(itemStack);
            if (slot > -1 || player == null)
                  return;

            List<ItemStack> stacks = stacks();
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

      public void serverPickBlock(ItemStack backpack, int index, ServerPlayer sender) {
            Inventory inventory = sender.getInventory();

            int freeSlot = inventory.getFreeSlot();
            if (freeSlot == -1)
                  return;


            if (freeSlot < 9)
                  inventory.selected = freeSlot;

            ItemStack selectedStack = inventory.getItem(inventory.selected);

            MutableItemStorage mutable = mutable();
            ItemStack take = mutable.removeItemNoUpdate(index);
            inventory.setItem(inventory.selected, take);
            freezeAndCancel(PatchedComponentHolder.of(backpack), mutable);
            sound().at(sender, ModSound.Type.REMOVE);

            int overflowSlot = -1;
            if (!selectedStack.isEmpty())
            {
                  overflowSlot = inventory.getFreeSlot();
                  inventory.setItem(overflowSlot, selectedStack);
            }

            sender.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, inventory.selected, selectedStack));
            sender.connection.send(new ClientboundSetCarriedItemPacket(inventory.selected));

//            Services.REGISTRY.triggerSpecial(sender, SpecialCriterion.Special.PICK_BACKPACK);

            if (overflowSlot > -1)
                  sender.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, overflowSlot, inventory.getItem(overflowSlot)));
      }

      public boolean pickupToBackpack(Player player, EquipmentSlot equipmentSlot, Inventory inventory, ItemStack backpack, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            if (!isFull()) {
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

                  MutableItemStorage mutable = mutable();
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
                        freezeAndCancel(PatchedComponentHolder.of(backpack), mutable);

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

      public boolean overflowFromInventory(EquipmentSlot equipmentSlot, Player player, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            MutableItemStorage mutable = mutable();
            ItemStack itemStack = mutable.addItem(stack, player);
            if (itemStack != null) {
                  ItemStack backpack = player.getItemBySlot(equipmentSlot);
                  freezeAndCancel(PatchedComponentHolder.of(backpack), mutable);
                  cir.setReturnValue(true);

                  if (player instanceof ServerPlayer serverPlayer) {
                        List<Pair<EquipmentSlot, ItemStack>> pSlots = List.of(Pair.of(equipmentSlot, backpack));
                        ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(serverPlayer.getId(), pSlots);
                        serverPlayer.serverLevel().getChunkSource().broadcast(serverPlayer, packet);
                  }

                  if (itemStack.isEmpty())
                        return true;
            }
            return false;
      }

      public <M extends MutableItemStorage> void freezeAndCancel(PatchedComponentHolder backpack, CallbackInfoReturnable<Boolean> cir, M mutable) {
            freezeAndCancel(backpack, mutable);
            cir.setReturnValue(true);
      }

      public <M extends MutableItemStorage> void freezeAndCancel(PatchedComponentHolder backpack, M mutable) {
            kind().freezeAndCancel(backpack, mutable);
      }

      public boolean canItemFit(ItemStack inserted) {
            return inserted.getItem().canFitInsideContainerItems() && Traits.get(inserted).isEmpty();
      }

      public void breakTrait(ServerPlayer pPlayer, ItemStack instance) {
            stacks().forEach(stack -> {
                  boolean success = pPlayer.getInventory().add(-1, stack);
                  if (!success || !stack.isEmpty()) {
                        pPlayer.drop(stack, true, true);
                  }
            });
      }

      public interface MutableItemStorage extends MutableTraits {

            int getMaxAmountToAdd(ItemStack stack);

            ItemStack removeItemNoUpdate(int slot);

            List<ItemStack> getItemStacks();

            @Override
            ItemStorageTraits trait();

            default ItemStack getSelectedStackSafe(Player player) {
                  int selectedSlot = trait().getSelectedSlotSafe(player);
                  return getItemStacks().get(selectedSlot);
            }
      }
}
