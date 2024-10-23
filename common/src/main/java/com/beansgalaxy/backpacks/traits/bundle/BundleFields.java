package com.beansgalaxy.backpacks.traits.bundle;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.IDeclaredFields;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class BundleFields extends IDeclaredFields {

      public static final StreamCodec<RegistryFriendlyByteBuf, BundleFields> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public void encode(RegistryFriendlyByteBuf buf, BundleFields fields) {
                  buf.writeInt(fields.size);
                  ModSound.STREAM_CODEC.encode(buf, fields.sound());
                  IDeclaredFields.encodeLocation(buf, fields);
            }

            @Override
            public BundleFields decode(RegistryFriendlyByteBuf buf) {
                  return new BundleFields(
                              buf.readInt(),
                              ModSound.STREAM_CODEC.decode(buf)
                  ).toReference(IDeclaredFields.decodeLocation(buf));
            }
      };

      private final int size;

      public BundleFields(@Nullable ResourceLocation location, int size, ModSound sound) {
            super(location, sound);
            this.size = size;
      }

      public BundleFields(int size, ModSound sound) {
            this(null, size, sound);
      }

      @Override
      public TraitComponentKind<? extends GenericTraits, ? extends IDeclaredFields> kind() {
            return Traits.BUNDLE;
      }

      @Override
      public BundleFields toReference(ResourceLocation location) {
            return new BundleFields(location, size, sound());
      }

      @Override
      @NotNull
      public GenericTraits asBlankTrait() {
            return new BundleTraits(this, List.of());
      }

      public int size() {
            return size;
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BundleFields that)) return false;
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
            return "BundleFields{" +
                        "size=" + size +
                        ", sound=" + sound() +
                        location().map(location ->
                                    ", location=" + location + '}')
                                    .orElse("}"
                        );
      }
}
