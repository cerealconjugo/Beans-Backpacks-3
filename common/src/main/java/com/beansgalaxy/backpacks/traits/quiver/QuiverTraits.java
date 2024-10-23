package com.beansgalaxy.backpacks.traits.quiver;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.SlotSelection;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;

public class QuiverTraits extends BundleLikeTraits {
      public static final String NAME = "quiver";
      private final QuiverFields fields;
      private final List<ItemStack> stacks;

      public QuiverTraits(QuiverFields fields, List<ItemStack> stacks) {
            super(Fraction.getFraction(stacks.size(), fields.size()));
            this.fields = fields;
            this.stacks = stacks;
      }

      public QuiverTraits(QuiverTraits traits, List<ItemStack> stacks) {
            this(traits.fields, traits.slotSelection, stacks);
      }

      public QuiverTraits(QuiverFields fields, SlotSelection selection, List<ItemStack> stacks) {
            super(Fraction.getFraction(stacks.size(), fields.size()), selection);
            this.fields = fields;
            this.stacks = stacks;
      }

      public static Optional<QuiverTraits> getQuiver(DataComponentHolder stack) {
            QuiverTraits quiverTraits = stack.get(Traits.QUIVER);
            if (quiverTraits != null)
                  return Optional.of(quiverTraits);

            ReferenceTrait referenceTrait = stack.get(Traits.REFERENCE);
            if (referenceTrait == null || referenceTrait.isEmpty())
                  return Optional.empty();

            return referenceTrait.getTrait().map(traits -> {
                  if (traits instanceof QuiverTraits storageTraits)
                        return storageTraits;
                  return null;
            });
      }

      public static void runIfQuiverEquipped(Player player, BiPredicate<QuiverTraits, EquipmentSlot> runnable) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                  ItemStack stack = player.getItemBySlot(slot);
                  if (stack.isEmpty())
                        continue;

                  Optional<QuiverTraits> traits = getQuiver(stack);
                  if (traits.isEmpty())
                        continue;

                  if (runnable.test(traits.get(), slot))
                        return;
            }
      }

      @Override
      public List<ItemStack> stacks() {
            return stacks;
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public QuiverFields fields() {
            return fields;
      }

      @Override
      public IClientTraits client() {
            return QuiverClient.INSTANCE;
      }

      @Override
      public QuiverTraits toReference(ResourceLocation location) {
            return new QuiverTraits(fields.toReference(location), slotSelection, stacks);
      }

      @Override
      public int size() {
            return fields.size();
      }

      @Override
      public Mutable mutable() {
            return new Mutable();
      }

      @Override
      public boolean isFull() {
            if (stacks.isEmpty()) {
                  return false;
            }

            if (stacks.size() < fields.size())
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
            Fraction weight = Traits.getWeight(stacks);
            int i = Fraction.getFraction(size(), 1).compareTo(weight);
            if (i > 0) {
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

      @Override
      public boolean overflowFromInventory(EquipmentSlot equipmentSlot, Player player, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            if (isFull())
                  return false;
            else
                  return super.overflowFromInventory(equipmentSlot, player, stack, cir);
      }

      @Override
      public boolean canItemFit(ItemStack inserted) {
            return canInsertProjectile(inserted.getItem()) && super.canItemFit(inserted);
      }

      public boolean canInsertProjectile(Item item) {
            return item instanceof ArrowItem;
      }

      public class Mutable extends BundleLikeTraits.MutableBundleLike {
            public Mutable() {
                  super(QuiverTraits.this);
            }

            @Nullable
            public ItemStack addItem(ItemStack inserted, int slot, @Nullable Player player) {
                  if (!canItemFit(inserted))
                        return null;

                  int i = fullness().compareTo(Fraction.ONE);
                  boolean hasSpace = i < 0;
                  if (!hasSpace && !inserted.isStackable())
                        return null;

                  int insertedCount = inserted.getCount();
                  int count = insertedCount;
                  for (ItemStack stored : getItemStacks()) {
                        if (inserted.isEmpty())
                              return ItemStack.EMPTY;

                        if (ItemStack.isSameItemSameComponents(stored, inserted)) {
                              int maxStackSize = stored.getMaxStackSize();
                              int storedCount = stored.getCount();
                              int insert = Math.min(maxStackSize - storedCount, count);
                              stored.grow(insert);
                              inserted.shrink(insert);
                              count -= insert;
                        }
                  }

                  if (!inserted.isEmpty() && getItemStacks().size() < fields.size()) {
                        ItemStack split = inserted.split(count);
                        count = 0;
                        getItemStacks().add(slot, split);
                  }

                  return insertedCount == count ? null : inserted;
            }

            @Override
            public QuiverTraits freeze() {
                  List<ItemStack> stacks = getItemStacks();
                  stacks.removeIf(ItemStack::isEmpty);
                  return new QuiverTraits(QuiverTraits.this, stacks);
            }

            @Override
            public void dropItems(Entity backpackEntity) {

            }

            @Override
            public InteractionResult interact(BackpackEntity backpackEntity, Player player, InteractionHand hand) {
                  ItemStack other = player.getItemInHand(hand);

                  if (other.isEmpty()) {
                        if (isEmpty() || InteractionHand.OFF_HAND.equals(hand))
                              return InteractionResult.PASS;

                        ItemStack stack = removeItemNoUpdate(other, player);
                        if (stack != null) {
                              player.setItemInHand(hand, stack);
                              sound().at(player, ModSound.Type.REMOVE);
                              backpackEntity.wobble = 8;
                              return InteractionResult.SUCCESS;
                        }
                  }
                  else if (addItem(other, player) == null) {
                        return InteractionResult.PASS;
                  }

                  backpackEntity.wobble = 8;
                  sound().at(player, ModSound.Type.INSERT);
                  return InteractionResult.SUCCESS;
            }

            @Override
            public QuiverTraits trait() {
                  return QuiverTraits.this;
            }
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof QuiverTraits traits)) return false;
            return Objects.equals(fields, traits.fields) && Objects.equals(stacks, traits.stacks);
      }

      @Override
      public int hashCode() {
            return Objects.hash(fields, stacks);
      }

      @Override
      public String toString() {
            return "QuiverTraits{" +
                        "fields=" + fields +
                        ", stacks=" + stacks +
                        '}';
      }
}
