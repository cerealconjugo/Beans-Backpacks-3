package com.beansgalaxy.backpacks.traits.alchemy;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.IDeclaredFields;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

public class AlchemyFields extends IDeclaredFields {
      private final int size;

      public AlchemyFields(int size, ModSound sound) {
            this(size, null, sound);
      }

      public AlchemyFields(int size, ResourceLocation location, ModSound sound) {
            super(location, sound);
            this.size = size;
      }

      public int size() {
            return size;
      }

      @Override
      public AlchemyTraits asBlankTrait() {
            return new AlchemyTraits(this, List.of());
      }

      @Override
      public TraitComponentKind<? extends GenericTraits, ? extends IDeclaredFields> kind() {
            return Traits.ALCHEMY;
      }

      @Override
      public AlchemyFields toReference(ResourceLocation location) {
            return new AlchemyFields(size, location, sound());
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AlchemyFields that)) return false;
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
            return "AlchemyFields{" +
                        "size=" + size +
                        ", sound=" + sound() +
                        location().map(location ->
                                    ", location=" + location + '}')
                                    .orElse("}"
                        );
      }
}
