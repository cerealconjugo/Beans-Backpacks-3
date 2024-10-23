package com.beansgalaxy.backpacks.traits.bulk;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.IDeclaredFields;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

class BulkFields extends IDeclaredFields {
      private final int size;

      public BulkFields(int size, ModSound sound) {
            this(null, size, sound);
      }

      public BulkFields(@Nullable ResourceLocation location, int size, ModSound sound) {
            super(location, sound);
            this.size = size;
      }

      @Override
      public TraitComponentKind<? extends GenericTraits, ? extends IDeclaredFields> kind() {
            return Traits.BULK;
      }

      @Override
      public BulkFields toReference(@Nullable ResourceLocation location) {
            return new BulkFields(location, size, sound());
      }

      @Override @NotNull
      public GenericTraits asBlankTrait() {
            return new BulkTraits(this, BuiltInRegistries.ITEM.wrapAsHolder(Items.AIR), List.of());
      }

      public int size() {
            return size;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, BulkFields> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public void encode(RegistryFriendlyByteBuf buf, BulkFields fields) {
                  buf.writeInt(fields.size);
                  ModSound.STREAM_CODEC.encode(buf, fields.sound());
                  IDeclaredFields.encodeLocation(buf, fields);
            }

            @Override @NotNull
            public BulkFields decode(RegistryFriendlyByteBuf buf) {
                  return new BulkFields(
                              buf.readInt(),
                              ModSound.STREAM_CODEC.decode(buf)
                  ).toReference(IDeclaredFields.decodeLocation(buf));
            }
      };

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BulkFields that)) return false;
            if (!super.equals(o)) return false;
            return size == that.size
                        && Objects.equals(location(), that.location())
                        && sound() == that.sound();
      }

      @Override
      public int hashCode() {
            return Objects.hash(super.hashCode(), size);
      }

      @Override
      public String toString() {
            return "BulkFields{" +
                        "size=" + size +
                        ", sound=" + sound() +
                        location().map(location ->
                                    ", location=" + location + '}')
                                    .orElse("}"
                        );
      }
}
