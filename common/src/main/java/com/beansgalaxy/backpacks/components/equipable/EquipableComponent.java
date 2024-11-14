package com.beansgalaxy.backpacks.components.equipable;

import com.beansgalaxy.backpacks.access.EquipmentSlotAccess;
import com.beansgalaxy.backpacks.components.EnderTraits;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public record EquipableComponent(EquipmentGroups slots, @Nullable EquipmentModel model, boolean traitRemovable) {
      public static final String NAME = "equipable";

      private EquipableComponent(EquipmentGroups slots, Optional<EquipmentModel> model, boolean traitRemovable) {
            this(slots, model.orElse(null), traitRemovable);
      }

      public static Optional<EquipableComponent> get(PatchedComponentHolder backpack) {
            if (backpack instanceof EnderTraits)
                  return Optional.empty();

            EquipableComponent equipable = backpack.get(Traits.EQUIPABLE);
            if (equipable != null)
                  return Optional.of(equipable);

            ReferenceTrait referenceTrait = backpack.get(Traits.REFERENCE);
            if (referenceTrait != null && !referenceTrait.isEmpty())
                  return referenceTrait.getEquipable();

            return Optional.empty();
      }

      public static Optional<EquipableComponent> get(ItemStack stack) {
            ReferenceTrait referenceTrait = stack.get(Traits.REFERENCE);
            if (referenceTrait != null && !referenceTrait.isEmpty())
                  return referenceTrait.getEquipable();

            EquipableComponent equipable = stack.get(Traits.EQUIPABLE);
            return Optional.ofNullable(equipable);
      }

      public static void runIfPresent(Player player, BiConsumer<EquipableComponent, EquipmentSlot> consumer) {
            for (EquipmentSlot value : EquipmentSlot.values()) {
                  ItemStack itemStack = player.getItemBySlot(value);
                  get(itemStack).ifPresent(equipable -> {
                        if (equipable.slots.test(value)) {
                              consumer.accept(equipable, value);
                        }
                  });
            }
      }

      public static boolean canEquip(PatchedComponentHolder backpack, Slot slot) {
            if (slot instanceof EquipmentSlotAccess access) {
                  return get(backpack).map(equipable -> {
                        EquipmentSlot accessSlot = access.getSlot();
                        return equipable.slots().test(accessSlot);
                  }).orElse(false);
            }

            return false;
      }

      public static void use(Player player, InteractionHand hand, ItemStack backpack, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
            get(backpack).ifPresent(equipable -> {
                  if (InteractionHand.OFF_HAND.equals(hand) && equipable.slots.test(EquipmentSlot.OFFHAND))
                        return;

                  EquipmentSlot slot = null;
                  for (EquipmentSlot value : EquipmentSlot.values()) {
                        if (!equipable.slots().test(value) || EquipmentSlot.OFFHAND.equals(value))
                              continue;

                        if (slot == null)
                              slot = value;

                        ItemStack equipped = player.getItemBySlot(value);
                        if (equipped.isEmpty()) {
                              player.setItemSlot(value, backpack);
                              player.setItemInHand(hand, ItemStack.EMPTY);
                              cir.setReturnValue(InteractionResultHolder.success(backpack));
                              return;
                        }
                  }

                  if (slot != null) {
                        ItemStack equipped = player.getItemBySlot(slot);
                        player.setItemSlot(slot, backpack);
                        player.setItemInHand(hand, equipped);
                        cir.setReturnValue(InteractionResultHolder.success(backpack));
                  }
            });
      }

      public static boolean testIfPresent(ItemStack backpack, Predicate<EquipableComponent> predicate) {
            return testIfPresent(PatchedComponentHolder.of(backpack), predicate);
      }

      public static boolean testIfPresent(PatchedComponentHolder backpack, Predicate<EquipableComponent> predicate) {
            Optional<EquipableComponent> optional = get(backpack);
            if (optional.isEmpty())
                  return false;

            EquipableComponent equipable = optional.get();
            return predicate.test(equipable);
      }

      public Optional<EquipmentModel> getModel() {
            return Optional.ofNullable(model);
      }

      public static final Codec<EquipableComponent> CODEC = RecordCodecBuilder.create(in ->
                  in.group(
                              EquipmentGroups.CODEC.fieldOf("slots").forGetter(EquipableComponent::slots),
                              EquipmentModel.CODEC.optionalFieldOf("model").forGetter(EquipableComponent::getModel),
                              Codec.BOOL.optionalFieldOf("trait_removable", false).forGetter(EquipableComponent::traitRemovable)
                  ).apply(in, EquipableComponent::new)
      );

      public static final StreamCodec<RegistryFriendlyByteBuf, EquipableComponent> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public EquipableComponent decode(RegistryFriendlyByteBuf buf) {
                  EquipmentGroups slotGroup = EquipmentGroups.STREAM_CODEC.decode(buf);

                  boolean noModel = buf.readBoolean();
                  EquipmentModel model = noModel
                              ? null
                              : EquipmentModel.STREAM_CODEC.decode(buf);

                  boolean traitRemovable = buf.readBoolean();
                  return new EquipableComponent(slotGroup, model, traitRemovable);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, EquipableComponent component) {
                  EquipmentGroups.STREAM_CODEC.encode(buf, component.slots);

                  EquipmentModel model = component.model;
                  boolean noModel = model == null;
                  buf.writeBoolean(noModel);
                  if (!noModel)
                        EquipmentModel.STREAM_CODEC.encode(buf, model);

                  buf.writeBoolean(component.traitRemovable);
            }
      };
}
