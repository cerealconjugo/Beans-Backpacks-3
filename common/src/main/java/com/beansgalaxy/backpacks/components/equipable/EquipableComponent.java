package com.beansgalaxy.backpacks.components.equipable;

import com.beansgalaxy.backpacks.access.EquipmentSlotAccess;
import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.util.OptionalEitherMapCodec;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public record EquipableComponent(EquipmentGroups slots, @Nullable EquipmentModel customModel, @Nullable ResourceLocation backpackTexture, boolean traitRemovable, @Nullable Holder<SoundEvent> sound) {
      public static final String NAME = "equipable";

      private EquipableComponent(EquipmentGroups slots, @Nullable EquipmentModel model, boolean traitRemovable, Optional<Holder<SoundEvent>> soundEvent) {
            this(slots, model, null, traitRemovable, soundEvent.orElse(null));
      }

      private EquipableComponent(EquipmentGroups slots, ResourceLocation texture, boolean traitRemovable, Optional<Holder<SoundEvent>> soundEvent) {
            this(slots, null, texture, traitRemovable, soundEvent.orElse(null));
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
                  if (equipable.traitRemovable)
                        return;

                  EquipmentSlot slot = null;
                  for (EquipmentSlot value : EquipmentSlot.values()) {
                        if (EquipmentSlot.Type.HAND.equals(value.getType()) || !equipable.slots().test(value))
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
            return Optional.ofNullable(customModel);
      }

      public static final OptionalEitherMapCodec<EquipmentModel, ResourceLocation> DISPLAY =
                  new OptionalEitherMapCodec<>(
                              "equipment_model", EquipmentModel.CODEC,
                              "backpack_texture", ResourceLocation.CODEC
                  );

      public static final Codec<EquipableComponent> CODEC = RecordCodecBuilder.create(in ->
                  in.group(
                              EquipmentGroups.CODEC.fieldOf("slots").forGetter(EquipableComponent::slots),
                              DISPLAY.forGetter(equipable -> {
                                    if (equipable.backpackTexture != null)
                                          return Optional.of(Either.right(equipable.backpackTexture));

                                    if (equipable.customModel != null)
                                          return Optional.of(Either.left(equipable.customModel));

                                    return Optional.empty();
                              }),
                              Codec.BOOL.optionalFieldOf("trait_removable", false).forGetter(EquipableComponent::traitRemovable),
                              SoundEvent.CODEC.optionalFieldOf("sound_event").forGetter(EquipableComponent::getSound)
                  ).apply(in, (slots, custom_model, trait_removable, sound_event) -> {
                        if (custom_model.isEmpty())
                              return new EquipableComponent(slots, null, null, trait_removable, sound_event.orElse(null));
                        Either<EquipmentModel, ResourceLocation> either = custom_model.get();
                        Optional<ResourceLocation> right = either.right();
                        if (right.isPresent())
                              return new EquipableComponent(slots, null, right.get(), trait_removable, sound_event.orElse(null));

                        Optional<EquipmentModel> left = either.left();
                        if (left.isPresent())
                              return new EquipableComponent(slots, left.get(), null, trait_removable, sound_event.orElse(null));

                        return new EquipableComponent(slots, null, null, trait_removable, sound_event.orElse(null));
                  })
      );

      private Optional<ResourceLocation> getTexture() {
            return Optional.ofNullable(backpackTexture);
      }

      public Optional<Holder<SoundEvent>> getSound() {
            return Optional.ofNullable(sound);
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, EquipableComponent> STREAM_CODEC = new StreamCodec<>() {

            @Override
            public void encode(@NotNull RegistryFriendlyByteBuf buf, EquipableComponent component) {
                  EquipmentGroups.STREAM_CODEC.encode(buf, component.slots);
                  buf.writeBoolean(component.traitRemovable);

                  EquipmentModel model = component.customModel;
                  boolean hasModel = model != null;
                  buf.writeBoolean(hasModel);
                  if (hasModel)
                        EquipmentModel.STREAM_CODEC.encode(buf, model);

                  ResourceLocation texture = component.backpackTexture;
                  boolean hasTexture = texture != null;
                  buf.writeBoolean(hasTexture);
                  if (hasTexture)
                        buf.writeResourceLocation(texture);

                  Holder<SoundEvent> sound = component.sound;
                  boolean hasSound = sound != null;
                  buf.writeBoolean(hasSound);
                  if (hasSound)
                        SoundEvent.STREAM_CODEC.encode(buf, component.sound);
            }

            @Override
            public EquipableComponent decode(RegistryFriendlyByteBuf buf) {
                  EquipmentGroups slotGroup = EquipmentGroups.STREAM_CODEC.decode(buf);
                  boolean traitRemovable = buf.readBoolean();

                  boolean hasModel = buf.readBoolean();
                  EquipmentModel model = hasModel
                              ? EquipmentModel.STREAM_CODEC.decode(buf)
                              : null;

                  boolean hasTexture = buf.readBoolean();
                  ResourceLocation texture = hasTexture
                              ? buf.readResourceLocation()
                              : null;

                  boolean hasSound = buf.readBoolean();
                  Holder<SoundEvent> sound = hasSound
                              ? SoundEvent.STREAM_CODEC.decode(buf)
                              : null;

                  return new EquipableComponent(slotGroup, model, texture, traitRemovable, sound);
            }
      };
}
