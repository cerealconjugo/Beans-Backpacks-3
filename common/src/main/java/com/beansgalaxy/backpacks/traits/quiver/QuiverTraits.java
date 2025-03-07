package com.beansgalaxy.backpacks.traits.quiver;

import com.beansgalaxy.backpacks.components.SlotSelection;
import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

public class QuiverTraits extends BundleLikeTraits {
      public static final String NAME = "quiver";

      public QuiverTraits(ModSound sound, int size) {
            super(sound, size);
      }

      public static Optional<QuiverTraits> getQuiver(DataComponentHolder stack) {
            QuiverTraits quiverTraits = stack.get(Traits.QUIVER);
            if (quiverTraits != null)
                  return Optional.of(quiverTraits);

            ReferenceTrait referenceTrait = stack.get(Traits.REFERENCE);
            if (referenceTrait == null)
                  return Optional.empty();

            return referenceTrait.getTrait().map(traits -> {
                  if (traits instanceof QuiverTraits storageTraits)
                        return storageTraits;
                  return null;
            });
      }

      public static void runIfQuiverEquipped(Player player, Function4<QuiverTraits, @Nullable EquipmentSlot, ItemStack, PatchedComponentHolder, Boolean> runnable) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                  ItemStack stack = player.getItemBySlot(slot);
                  if (stack.isEmpty())
                        continue;

                  Optional<QuiverTraits> traits = getQuiver(stack);
                  QuiverTraits trait;
                  PatchedComponentHolder holder;
                  if (traits.isEmpty()) {
                        Optional<EnderTraits> optionalEnder = EnderTraits.get(stack);
                        if (optionalEnder.isEmpty()) {
                              continue;
                        }

                        EnderTraits ender = optionalEnder.get();
                        GenericTraits generic = ender.getTrait(player.level());
                        if (generic instanceof QuiverTraits quiverTraits) {
                              trait = quiverTraits;
                              holder = ender;
                        }
                        else continue;
                  }
                  else {
                        trait = traits.get();
                        holder = PatchedComponentHolder.of(stack);
                  }

                  if (runnable.apply(trait, slot, stack, holder))
                        return;
            }

            for (ItemStack stack : player.getInventory().items) {
                  if (stack.isEmpty())
                        continue;

                  Optional<EquipableComponent> equipableComponent = EquipableComponent.get(stack);
                  if (equipableComponent.isPresent())
                        continue;

                  Optional<QuiverTraits> traits = getQuiver(stack);
                  QuiverTraits trait;
                  PatchedComponentHolder holder;
                  if (traits.isEmpty()) {
                        Optional<EnderTraits> optionalEnder = EnderTraits.get(stack);
                        if (optionalEnder.isEmpty()) {
                              continue;
                        }

                        EnderTraits ender = optionalEnder.get();
                        GenericTraits generic = ender.getTrait(player.level());
                        if (generic instanceof QuiverTraits quiverTraits) {
                              trait = quiverTraits;
                              holder = ender;
                        }
                        else continue;
                  }
                  else {
                        trait = traits.get();
                        holder = PatchedComponentHolder.of(stack);
                  }

                  if (runnable.apply(trait, null, stack, holder))
                        return;
            }
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public QuiverClient client() {
            return QuiverClient.INSTANCE;
      }

      @Override
      public QuiverEntity entity() {
            return QuiverEntity.INSTANCE;
      }

      @Override
      public boolean isFull(PatchedComponentHolder holder) {
            List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
            if (stacks == null || stacks.isEmpty() || stacks.size() < size())
                  return false;

            for (ItemStack stack : stacks) {
                  int maxStackSize = stack.getMaxStackSize();
                  int count = stack.getCount();
                  if (maxStackSize != count)
                        return false;
            }

            return true;
      }

      public boolean pickupToBackpack(Player player, EquipmentSlot equipmentSlot, Inventory inventory, ItemStack backpack, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            List<ItemStack> stacks = backpack.get(ITraitData.ITEM_STACKS);
            Fraction fraction = stacks == null || stacks.isEmpty()
                        ? Fraction.ZERO
                        : Traits.getWeight(stacks);

            int i = Fraction.getFraction(size(), 1).compareTo(fraction);
            if (i > 0) {
                  QuiverMutable mutable = this.mutable(PatchedComponentHolder.of(backpack));
                  if (mutable.addItem(stack, player) != null) {
                        cir.setReturnValue(true);
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
      public boolean overflowFromInventory(EquipmentSlot equipmentSlot, Player player, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            ItemStack backpack = player.getItemBySlot(equipmentSlot);
            if (isFull(backpack))
                  return false;
            else
                  return super.overflowFromInventory(equipmentSlot, player, stack, cir);
      }

      @Override
      public boolean canItemFit(PatchedComponentHolder holder, ItemStack inserted) {
            return canInsertProjectile(inserted.getItem()) && super.canItemFit(holder, inserted);
      }

      public boolean canInsertProjectile(Item item) {
            return item instanceof ArrowItem;
      }

      @Override
      public QuiverMutable mutable(PatchedComponentHolder holder) {
            return new QuiverMutable(this, holder);
      }

      @Override
      public String toString() {
            return "QuiverTraits{" +
                        "size=" + size() +
                        ", sound=" + sound() +
                        '}';
      }

      @Override
      public TraitComponentKind<? extends GenericTraits> kind() {
            return Traits.QUIVER;
      }
}
