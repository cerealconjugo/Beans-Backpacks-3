package com.beansgalaxy.backpacks.traits.chest;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.IDeclaredFields;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public class ChestFields extends IDeclaredFields {
      final int rows;
      final int columns;

      public ChestFields(int rows, int columns, ModSound sound) {
            this(null, sound, rows, columns);
      }
      public ChestFields(ResourceLocation location, ModSound sound, int rows, int columns) {
            super(location, sound);
            this.rows = rows;
            this.columns = columns;
      }

      @Override
      public ChestTraits asBlankTrait() {
            return new ChestTraits(this, new Int2ObjectArrayMap<>(rows * columns));
      }

      @Override
      public TraitComponentKind<? extends GenericTraits, ? extends IDeclaredFields> kind() {
            return Traits.CHEST;
      }

      @Override
      public ChestFields toReference(ResourceLocation location) {
            return new ChestFields(location, sound(), rows, columns);
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChestFields that)) return false;
            if (!super.equals(o)) return false;
            return rows == that.rows && columns == that.columns
                        && Objects.equals(location(), that.location())
                        && sound() == that.sound();
      }

      @Override
      public int hashCode() {
            return Objects.hash(super.hashCode(), rows, columns);
      }

      @Override
      public String toString() {
            return "ChestFields{" +
                          "rows=" + rows +
                        ", columns=" + columns +
                        ", sound=" + sound() +
                        location().map(location ->
                                    ", location=" + location + '}')
                                    .orElse("}"
                        );
      }

      public int rows() {
            return rows;
      }

      public int columns() {
            return columns;
      }
}
