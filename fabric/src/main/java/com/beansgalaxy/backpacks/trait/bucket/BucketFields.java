package com.beansgalaxy.backpacks.trait.bucket;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.IDeclaredFields;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public class BucketFields extends IDeclaredFields {
      private final int size;

      public BucketFields(ResourceLocation location, ModSound sound, int size) {
            super(location, sound);
            this.size = size;
      }

      public BucketFields(int size, ModSound sound) {
            this(null, sound, size);
      }

      public int size() {
            return size;
      }

      @Override
      public BucketTraits asBlankTrait() {
            return new BucketTraits(this, FluidVariant.blank(), 0);
      }

      @Override
      public TraitComponentKind<? extends GenericTraits, ? extends IDeclaredFields> kind() {
            return Traits.BUCKET;
      }

      @Override
      public BucketFields toReference(ResourceLocation location) {
            return new BucketFields(location, sound(), size);
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, BucketFields> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public void encode(RegistryFriendlyByteBuf buf, BucketFields fields) {
                  buf.writeInt(fields.size);
                  ModSound.STREAM_CODEC.encode(buf, fields.sound());
                  IDeclaredFields.encodeLocation(buf, fields);
            }

            @Override
            public BucketFields decode(RegistryFriendlyByteBuf buf) {
                  return new BucketFields(
                              buf.readInt(),
                              ModSound.STREAM_CODEC.decode(buf)
                  ).toReference(IDeclaredFields.decodeLocation(buf));
            }
      };

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BucketFields that)) return false;
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
            return "BucketFields{" +
                        "size=" + size +
                        ", sound=" + sound() +
                        location().map(location ->
                                    ", location=" + location + '}')
                                    .orElse("}"
                        );
      }
}
