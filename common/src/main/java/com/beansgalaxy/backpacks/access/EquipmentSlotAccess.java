package com.beansgalaxy.backpacks.access;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlot;

public interface EquipmentSlotAccess {
      EquipmentSlot getSlot();

      StreamCodec<RegistryFriendlyByteBuf, EquipmentSlot> EQUIPMENT_SLOT_STREAM_CODEC = new StreamCodec<>() {
            @Override
            public EquipmentSlot decode(RegistryFriendlyByteBuf buf) {
                  byte b = buf.readByte();
                  for (EquipmentSlot value : EquipmentSlot.values()) {
                        if (value.getFilterFlag() == (int) b) {
                              return value;
                        }
                  }
                  return EquipmentSlot.MAINHAND;
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, EquipmentSlot equipmentSlot) {
                  byte i = (byte) equipmentSlot.getFilterFlag();
                  buf.writeByte(i);
            }
      };
}
