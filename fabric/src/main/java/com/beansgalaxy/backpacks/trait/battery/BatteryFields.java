package com.beansgalaxy.backpacks.trait.battery;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.IDeclaredFields;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BatteryFields extends IDeclaredFields {
      private final int size;
      private final long speed;

      public BatteryFields(int size, long speed, ModSound sound) {
            this(size, speed, null, sound);
      }

      public BatteryFields(int size, long speed, ResourceLocation location, ModSound sound) {
            super(location, sound);
            this.size = size;
            this.speed = speed;
      }

      public int size() {
            return size;
      }

      public long speed() {
            return speed;
      }

      @Override
      public BatteryTraits asBlankTrait() {
            return new BatteryTraits(this, ItemStack.EMPTY, 0);
      }

      @Override
      public TraitComponentKind<? extends GenericTraits, ? extends IDeclaredFields> kind() {
            return Traits.BATTERY;
      }

      @Override
      public BatteryFields toReference(ResourceLocation location) {
            return new BatteryFields(size, speed, location, sound());
      }


      public static final StreamCodec<RegistryFriendlyByteBuf, BatteryFields> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public void encode(RegistryFriendlyByteBuf buf, BatteryFields fields) {
                  buf.writeInt(fields.size);
                  buf.writeLong(fields.speed);
                  ModSound.STREAM_CODEC.encode(buf, fields.sound());
                  IDeclaredFields.encodeLocation(buf, fields);
            }

            @Override @NotNull
            public BatteryFields decode(RegistryFriendlyByteBuf buf) {
                  return new BatteryFields(
                              buf.readInt(),
                              buf.readLong(),
                              ModSound.STREAM_CODEC.decode(buf)
                  ).toReference(IDeclaredFields.decodeLocation(buf));
            }
      };

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BatteryFields that)) return false;
            if (!super.equals(o)) return false;
            return size == that.size && speed == that.speed
                        && Objects.equals(location(), that.location())
                        && sound() == that.sound();
      }

      @Override
      public int hashCode() {
            return Objects.hash(super.hashCode(), size, speed);
      }

      @Override
      public String toString() {
            return "BatteryFields{" +
                        "size=" + size +
                        ", speed=" + speed +
                        ", sound=" + sound() +
                        location().map(location ->
                                    ", location=" + location + '}')
                                    .orElse("}"
                        );
      }
}
