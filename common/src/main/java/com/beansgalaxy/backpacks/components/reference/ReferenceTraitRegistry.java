package com.beansgalaxy.backpacks.components.reference;

import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.HashMap;

public class ReferenceTraitRegistry {
      public static final HashMap<ResourceLocation, ReferenceFields> REFERENCES = new HashMap<>();

      public static ReferenceFields get(ResourceLocation location) {
            ReferenceFields reference = REFERENCES.get(location);
            if (reference == null)
                  return new ReferenceFields(NonTrait.INSTANCE, ItemAttributeModifiers.EMPTY, null, null);

            return reference;
      }

      public static void put(ResourceLocation location, ReferenceFields referenceFields) {
            REFERENCES.put(location, referenceFields);
      }

}
