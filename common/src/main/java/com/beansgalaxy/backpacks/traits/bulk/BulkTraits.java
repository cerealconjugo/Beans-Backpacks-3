package com.beansgalaxy.backpacks.traits.bulk;

import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.network.serverbound.PickBlock;
import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.screen.TinyClickType;
import com.beansgalaxy.backpacks.traits.*;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class BulkTraits extends ItemStorageTraits {
      public static final String NAME = "bulk";
      private final int size;

      public BulkTraits(@Nullable ResourceLocation location, ModSound sound, int size) {
            super(location, sound);
            this.size = size;
      }

      @Override
      public BulkClient client() {
            return BulkClient.INSTANCE;
      }

      @Override
      public BulkEntity entity() {
            return BulkEntity.INSTANCE;
      }

      @Override
      public BulkTraits toReference(ResourceLocation location) {
            return new BulkTraits(location, sound(), size);
      }

      @Override
      public String name() {
            return NAME;
      }

      public int size() {
            return size;
      }

      @Override
      public Fraction fullness(PatchedComponentHolder holder) {
            BulkMutable.BulkStacks bulkStacks = holder.get(ITraitData.BULK_STACKS);
            if (bulkStacks == null || bulkStacks.isEmpty())
                  return Fraction.ZERO;

            return bulkStacks.fullness(this);
      }

      @Override
      public void stackedOnMe(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {
            BulkMutable mutable = newMutable(backpack);
            if (!EquipableComponent.testIfPresent(backpack, equipable -> !equipable.traitRemovable())) {
                  if (ClickAction.SECONDARY.equals(click)) {
                        if (other.isEmpty()) {
                              if (mutable.isEmpty())
                                    return;

                              ItemStack stack = mutable.removeItem(0);
                              access.set(stack);
                              sound().atClient(player, ModSound.Type.REMOVE);
                        } else if (mutable.addItem(other, player) != null) {
                              sound().atClient(player, ModSound.Type.INSERT);
                        }
                  }
            }
            else if (EquipableComponent.canEquip(backpack, slot)) {
                  if (other.isEmpty()) {
                        if (mutable.isEmpty())
                              return;

                        ItemStack stack = ClickAction.SECONDARY.equals(click)
                                    ? mutable.splitItem()
                                    : mutable.removeItem(0);

                        access.set(stack);
                        sound().atClient(player, ModSound.Type.REMOVE);
                  } else {
                        ItemStack returned;
                        if (ClickAction.PRIMARY.equals(click))
                              returned = mutable.addItem(other, player);
                        else {
                              returned = mutable.addItem(other.copyWithCount(1), player);
                              if (returned != null)
                                    other.shrink(1);
                        }

                        if (returned == null) {
                              return;
                        }

                        sound().atClient(player, ModSound.Type.INSERT);
                  }
            }
            else return;

            mutable.push(cir);
      }

      @Override
      public void stackedOnOther(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir) {
            boolean empty = !EquipableComponent.testIfPresent(backpack, equipable ->
                        !equipable.slots().test(EquipmentSlot.OFFHAND)
            );

            if (empty && ClickAction.SECONDARY.equals(click)) {
                  BulkMutable mutable = newMutable(backpack);
                  ModSound sound = sound();
                  if (other.isEmpty()) {
                        ItemStack stack = mutable.removeItem(0);
                        if (stack.isEmpty() || !slot.mayPlace(stack))
                              return;

                        slot.set(stack);
                        sound.atClient(player, ModSound.Type.REMOVE);
                  }
                  else if (slot.mayPickup(player)) {
                        if (mutable.addItem(other, player) != null)
                              sound.atClient(player, ModSound.Type.INSERT);
                        else return;
                  }
                  else return;

                  cir.setReturnValue(true);
                  mutable.push();
            }
      }

      static List<ItemStack> stacks(BulkMutable.BulkStacks bulkStacks) {
            return stacks(bulkStacks.itemHolder(), bulkStacks.emptyStacks());
      }

      static List<ItemStack> stacks(Holder<Item> item, List<BulkMutable.EmptyStack> emptyStacks) {
            return emptyStacks.stream().map(
                        empty -> new ItemStack(item, empty.amount(), empty.data())
            ).toList();
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, BulkTraits> STREAM_CODEC = StreamCodec.of((buf, traits) -> {
            buf.writeInt(traits.size());
            ModSound.STREAM_CODEC.encode(buf, traits.sound());
            GenericTraits.encodeLocation(buf, traits);
      }, buf -> new BulkTraits(
                  GenericTraits.decodeLocation(buf),
                  ModSound.STREAM_CODEC.decode(buf),
                  buf.readInt()
      ));

      @Override
      public void hotkeyUse(Slot slot, EquipmentSlot selectedEquipment, int button, ClickType actionType, Player player, CallbackInfo ci) {
            if (selectedEquipment == null) {
                  PatchedComponentHolder holder = PatchedComponentHolder.of(slot.getItem());
                  BulkMutable mutable = newMutable(holder);
                  if (mutable.isEmpty()) {
                        ci.cancel();
                        return;
                  }

                  Inventory inventory = player.getInventory();
                  if (ClickType.PICKUP_ALL.equals(actionType)) {
                        ItemStack carried = player.containerMenu.getCarried();
                        if (!canItemFit(holder, carried)) {
                              ci.cancel();
                              return;
                        }

                        BulkMutable.BulkStacks bulkList = mutable.bulkList.get();
                        Holder<Item> itemHolder = bulkList.itemHolder();
                        List<BulkMutable.EmptyStack> emptyStacks = bulkList.emptyStacks();

                        Iterator<BulkMutable.EmptyStack> iterator = emptyStacks.iterator();
                        BulkMutable.EmptyStack stack = iterator.next();
                        ItemStack pStack = stack.withItem(itemHolder);
                        int stackableSlot = inventory.getSlotWithRemainingSpace(pStack);
                        if (stackableSlot == -1) {
                              stackableSlot = inventory.getFreeSlot();
                        }
                        if (stackableSlot != -1) {
                              ci.cancel();
                              while (iterator.hasNext()) {
                                    int maxStackSize = stack.getMaxStackSize(itemHolder);
                                    ItemStack splitStack = stack.splitItem(itemHolder, maxStackSize);
                                    do {
                                          if (!inventory.add(-1, splitStack)) {
                                                emptyStacks.addFirst(new BulkMutable.EmptyStack(splitStack.getCount(), splitStack.getComponentsPatch()));
                                                return;
                                          }
                                          splitStack = stack.splitItem(itemHolder, maxStackSize);
                                    }
                                    while (!stack.isEmpty());

                                    iterator.remove();
                                    stack = iterator.next();
                              }
                        }

                        boolean cancelled = ci.isCancelled();
                        if (cancelled) {
                              sound().atClient(player, ModSound.Type.REMOVE);
                              mutable.bulkList.set(bulkList);
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
                  if (isFull(backpack))
                        return;

                  PatchedComponentHolder holder = PatchedComponentHolder.of(backpack);
                  BulkMutable mutable = newMutable(holder);
                  if (ClickType.PICKUP_ALL.equals(actionType)) {
                        ItemStack carried = player.containerMenu.getCarried();
                        if (mutable.isEmpty() || !carried.is(mutable.bulkList.get().itemHolder())) {
                              ci.cancel();
                              return;
                        }

                        Inventory inventory = player.getInventory();
                        NonNullList<ItemStack> items = inventory.items;
                        for (int i = items.size() - 1; i >= 0  && !mutable.isFull(); i--) {
                              ItemStack stack = items.get(i);
                              if (ItemStack.isSameItem(carried, stack)) {
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
                        ItemStack slotItem = slot.getItem();
                        if (mutable.addItem(slotItem, player) != null) {
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

            BulkMutable mutable = newMutable(backpack);

            ItemStack removed;
            if (menuKeyDown)
                  removed = mutable.removeItem(0);
            else if (EquipableComponent.get(backpack).isPresent())
            {
                  BulkMutable.BulkStacks bulkList = mutable.bulkList.get();
                  List<BulkMutable.EmptyStack> emptyStacks = bulkList.emptyStacks();
                  BulkMutable.EmptyStack first = emptyStacks.getFirst();
                  ItemStack stack = first.splitItem(bulkList.itemHolder(), 1);
                  if (first.isEmpty())
                        emptyStacks.removeFirst();

                  mutable.bulkList.set(bulkList);
                  removed = stack;
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

                  BulkMutable mutable = newMutable(PatchedComponentHolder.of(backpack));
                  BulkMutable.BulkStacks bulkStacks = mutable.bulkList.get();
                  Holder<Item> itemHolder = bulkStacks.itemHolder();
                  if (stack.is(itemHolder)) {
                        ItemStack returnStack = mutable.addItem(stack, player);
                        if (returnStack != null) {
                              cir.setReturnValue(true);
                              sound().toClient(player, ModSound.Type.INSERT, 1, 1);
                              mutable.push();

                              if (player instanceof ServerPlayer serverPlayer) {
                                    List<Pair<EquipmentSlot, ItemStack>> pSlots = List.of(Pair.of(equipmentSlot, backpack));
                                    ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(serverPlayer.getId(), pSlots);
                                    serverPlayer.serverLevel().getChunkSource().broadcastAndSend(serverPlayer, packet);
                              }
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
            BulkMutable.BulkStacks bulkStacks = backpack.get(ITraitData.BULK_STACKS);
            if (bulkStacks == null || !itemStack.is(bulkStacks.itemHolder()))
                  return;

            PickBlock.send(0, equipmentSlot);
            sound().atClient(player, ModSound.Type.REMOVE);
            ci.cancel();
      }

      @Override
      public void breakTrait(ServerPlayer pPlayer, ItemStack instance) {
            BulkMutable.BulkStacks bulkStacks = instance.get(ITraitData.BULK_STACKS);
            if (bulkStacks == null)
                  return;

            stacks(bulkStacks).forEach(stack -> {
                  boolean success = pPlayer.getInventory().add(-1, stack);
                  if (!success || !stack.isEmpty()) {
                        pPlayer.drop(stack, true, true);
                  }
            });
      }

      @Override @Nullable
      public ItemStack getFirst(PatchedComponentHolder backpack) {
            BulkMutable.BulkStacks bulkStacks = backpack.get(ITraitData.BULK_STACKS);
            if (bulkStacks == null)
                  return null;

            BulkMutable.EmptyStack first = bulkStacks.emptyStacks().getFirst();
            Holder<Item> itemHolder = bulkStacks.itemHolder();
            return first.withCappedStackSize(itemHolder);
      }

      @Override
      public void tinyMenuClick(PatchedComponentHolder holder, int index, TinyClickType clickType, SlotAccess carriedAccess, Player player) {

      }

      @Override
      public boolean isEmpty(PatchedComponentHolder holder) {
            return !holder.has(ITraitData.BULK_STACKS);
      }

      @Override
      public boolean canItemFit(PatchedComponentHolder holder, ItemStack inserted) {
            if (!super.canItemFit(holder, inserted))
                  return false;

            if (isEmpty(holder))
                  return true;

            BulkMutable.BulkStacks bulkStacks = holder.get(ITraitData.BULK_STACKS);
            return bulkStacks != null && inserted.is(bulkStacks.itemHolder());
      }

      @Override
      public BulkMutable newMutable(PatchedComponentHolder holder) {
            return new BulkMutable(this, holder);
      }

      @Override
      public TraitComponentKind<? extends GenericTraits> kind() {
            return Traits.BULK;
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BulkTraits that)) return false;
            return size() == that.size() && Objects.equals(sound(), that.sound()) && Objects.equals(location(), that.location());
      }

      @Override
      public int hashCode() {
            return Objects.hash(size(), sound(), location());
      }

      @Override
      public String toString() {
            return "BulkTraits{" +
                        "size=" + size() +
                        ", sound=" + sound() +
                        location().map(
                                    location -> ", location=" + location + '}')
                                    .orElse("}");
      }
}
