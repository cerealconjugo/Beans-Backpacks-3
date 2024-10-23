package com.beansgalaxy.backpacks.components.equipable;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;

import java.util.function.IntFunction;
import java.util.function.Predicate;

public enum EquipmentGroups implements StringRepresentable {
      FEET(0, "feet", EquipmentSlot.FEET, EquipmentSlotGroup.FEET),
      LEGS(1, "legs", EquipmentSlot.LEGS, EquipmentSlotGroup.LEGS),
      CHEST(2, "chest", EquipmentSlot.CHEST, EquipmentSlotGroup.CHEST),
      HEAD(3, "head", EquipmentSlot.HEAD, EquipmentSlotGroup.HEAD),
      ARMOR(4, "armor", slot -> EquipmentSlot.Type.HUMANOID_ARMOR.equals(slot.getType()), EquipmentSlotGroup.ARMOR),
      BODY(5, "body", EquipmentSlot.BODY, EquipmentSlotGroup.BODY),
      TORSO(6, "torso", slot -> EquipmentSlot.BODY.equals(slot) || EquipmentSlot.CHEST.equals(slot), EquipmentSlotGroup.BODY),
      OFFHAND(7, "offhand", EquipmentSlot.OFFHAND, EquipmentSlotGroup.OFFHAND),
      AT_READY(8, "at_ready", slot -> EquipmentSlot.BODY.equals(slot) || EquipmentSlot.OFFHAND.equals(slot), EquipmentSlotGroup.BODY);

      public static final IntFunction<EquipmentGroups> BY_ID = ByIdMap.continuous(EquipmentGroups::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
      public static final Codec<EquipmentGroups> CODEC = StringRepresentable.fromEnum(EquipmentGroups::values);
      public static final StreamCodec<ByteBuf, EquipmentGroups> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, EquipmentGroups::getId);

      private final int id;
      private final String name;
      private final Predicate<EquipmentSlot> predicate;
      private final Predicate<EquipmentSlotGroup> groupPredicate;

      EquipmentGroups(int id, String name, EquipmentSlot equipmentSlot, EquipmentSlotGroup slotGroup) {
            this(id, name, equipmentSlot::equals, slotGroup);
      }

      EquipmentGroups(int id, String name, Predicate<EquipmentSlot> predicate, EquipmentSlotGroup slotGroup) {
            this.id = id;
            this.name = name;
            this.predicate = predicate;
            this.groupPredicate = slotGroup::equals;
      }

      @Override
      public String getSerializedName() {
            return name;
      }

      public boolean test(EquipmentSlot slot) {
            return predicate.test(slot);
      }

      public boolean test(EquipmentSlotGroup slot) {
            return groupPredicate.test(slot);
      }

      public int getId() {
            return id;
      }
}
