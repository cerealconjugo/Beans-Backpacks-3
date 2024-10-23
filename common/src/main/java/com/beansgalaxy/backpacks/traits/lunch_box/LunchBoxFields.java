package com.beansgalaxy.backpacks.traits.lunch_box;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.IDeclaredFields;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class LunchBoxFields extends IDeclaredFields {
      private final int size;

      protected LunchBoxFields(int size, ModSound sound) {
            this(null, size, sound);
      }

      protected LunchBoxFields(ResourceLocation location, int size, ModSound sound) {
            super(location, sound);
            this.size = size;
      }

      @Override
      public TraitComponentKind<? extends GenericTraits, ? extends IDeclaredFields> kind() {
            return Traits.LUNCH_BOX;
      }

      @Override
      @NotNull
      public GenericTraits asBlankTrait() {
            return new LunchBoxTraits(this, List.of(), List.of());
      }

      public int size() {
            return size;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, LunchBoxFields> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public void encode(RegistryFriendlyByteBuf buf, LunchBoxFields fields) {
                  buf.writeInt(fields.size());
                  ModSound.STREAM_CODEC.encode(buf, fields.sound());
                  IDeclaredFields.encodeLocation(buf, fields);
            }

            @Override
            public LunchBoxFields decode(RegistryFriendlyByteBuf buf) {
                  return new LunchBoxFields(
                              buf.readInt(),
                              ModSound.STREAM_CODEC.decode(buf)
                  ).toReference(IDeclaredFields.decodeLocation(buf));
            }
      };

      @Override
      public LunchBoxFields toReference(ResourceLocation location) {
            return new LunchBoxFields(location, size, sound());
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LunchBoxFields that)) return false;
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
            return "LunchBoxFields{" +
                        "size=" + size +
                        ", sound=" + sound() +
                        location().map(location ->
                                    ", location=" + location + '}')
                                    .orElse("}"
                        );
      }
}
