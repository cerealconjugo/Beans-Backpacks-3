package com.beansgalaxy.backpacks.traits.generic;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class ItemStorageTraits extends GenericTraits {

      public ItemStorageTraits(ResourceLocation location, ModSound sound) {
            super(location, sound);
      }

      private static Optional<ItemStorageTraits> get(DataComponentHolder stack) {
            ReferenceTrait referenceTrait = stack.get(Traits.REFERENCE);
            if (referenceTrait != null && !referenceTrait.isEmpty())
                  return referenceTrait.getTrait().map(traits -> {
                        if (traits instanceof ItemStorageTraits storageTraits)
                              return storageTraits;
                        return null;
                  });

            for (TraitComponentKind<? extends ItemStorageTraits> type : Traits.STORAGE_TRAITS) {
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

      public abstract MutableItemStorage newMutable(PatchedComponentHolder holder);

      public abstract void hotkeyUse(Slot slot, EquipmentSlot selectedEquipment, int button, ClickType actionType, Player player, CallbackInfo ci);

      public abstract void hotkeyThrow(Slot slot, PatchedComponentHolder backpack, int button, Player player, boolean menuKeyDown, CallbackInfo ci);

      public abstract boolean pickupToBackpack(Player player, EquipmentSlot equipmentSlot, Inventory inventory, ItemStack backpack, ItemStack stack, CallbackInfoReturnable<Boolean> cir);

      public abstract void clientPickBlock(EquipmentSlot equipmentSlot, boolean instantBuild, Inventory inventory, ItemStack itemStack, Player player, CallbackInfo ci);

      public void serverPickBlock(ItemStack backpack, int index, ServerPlayer sender) {
            Inventory inventory = sender.getInventory();

            int freeSlot = inventory.getFreeSlot();
            if (freeSlot == -1)
                  return;


            if (freeSlot < 9)
                  inventory.selected = freeSlot;

            ItemStack selectedStack = inventory.getItem(inventory.selected);

            MutableItemStorage mutable = newMutable(PatchedComponentHolder.of(backpack));
            ItemStack take = mutable.removeItem(index);
            inventory.setItem(inventory.selected, take);
            mutable.push();
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

      public boolean overflowFromInventory(EquipmentSlot equipmentSlot, Player player, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            ItemStack backpack = player.getItemBySlot(equipmentSlot);
            MutableItemStorage mutable = newMutable(PatchedComponentHolder.of(backpack));
            ItemStack itemStack = mutable.addItem(stack, player);
            if (itemStack != null) {
                  mutable.push();
                  cir.setReturnValue(true);

                  if (player instanceof ServerPlayer serverPlayer) {
                        List<Pair<EquipmentSlot, ItemStack>> pSlots = List.of(Pair.of(equipmentSlot, backpack));
                        ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(serverPlayer.getId(), pSlots);
                        serverPlayer.serverLevel().getChunkSource().broadcast(serverPlayer, packet);
                  }

                  return itemStack.isEmpty();
            }
            return false;
      }

      public boolean canItemFit(PatchedComponentHolder holder, ItemStack inserted) {
            return inserted.getItem().canFitInsideContainerItems() && Traits.get(inserted).isEmpty();
      }

      public abstract void breakTrait(ServerPlayer pPlayer, ItemStack instance);

      @Nullable
      public abstract ItemStack getFirst(PatchedComponentHolder backpack);
}
