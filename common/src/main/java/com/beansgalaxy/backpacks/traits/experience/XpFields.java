package com.beansgalaxy.backpacks.traits.experience;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.IDeclaredFields;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class XpFields extends IDeclaredFields {
      private final int size;
      final int points;

      public XpFields(int size, ModSound sound) {
            this(size, null, sound);
      }

      public XpFields(int size, ResourceLocation location, ModSound sound) {
            super(location, sound);
            this.size = size;
            this.points = XpTraits.pointsFromLevels(size);
      }

      public int size() {
            return size;
      }

      @Override
      public XpTraits asBlankTrait() {
            return new XpTraits(this, 0);
      }

      @Override
      public TraitComponentKind<? extends GenericTraits, ? extends IDeclaredFields> kind() {
            return Traits.EXPERIENCE;
      }

      @Override
      public XpFields toReference(ResourceLocation location) {
            return new XpFields(size, location, sound());
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, XpFields> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public void encode(RegistryFriendlyByteBuf buf, XpFields fields) {
                  buf.writeInt(fields.size);
                  ModSound.STREAM_CODEC.encode(buf, fields.sound());
                  IDeclaredFields.encodeLocation(buf, fields);
            }

            @Override @NotNull
            public XpFields decode(RegistryFriendlyByteBuf buf) {
                  return new XpFields(
                              buf.readInt(),
                              ModSound.STREAM_CODEC.decode(buf)
                  ).toReference(IDeclaredFields.decodeLocation(buf));
            }
      };

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof XpFields that)) return false;
            if (!super.equals(o)) return false;
            return size == that.size && points == that.points
                        && Objects.equals(location(), that.location())
                        && sound() == that.sound();
      }

      @Override
      public int hashCode() {
            return Objects.hash(super.hashCode(), size, points);
      }

      @Override
      public String toString() {
            return "XpFields{" +
                        "size=" + size +
                        ", points=" + points +
                        ", sound=" + sound() +
                        location().map(location ->
                                    ", location=" + location + '}')
                                    .orElse("}"
                        );
      }
}
