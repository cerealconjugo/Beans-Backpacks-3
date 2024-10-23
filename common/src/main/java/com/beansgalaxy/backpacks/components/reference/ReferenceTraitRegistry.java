package com.beansgalaxy.backpacks.components.reference;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class ReferenceTraitRegistry {
      public static final HashMap<ResourceLocation, ReferenceFields> REFERENCES = new HashMap<>();

      @Nullable
      public static ReferenceFields get(ResourceLocation location) {
            return REFERENCES.getOrDefault(location, new ReferenceFields(NonTrait.INSTANCE,
                        ItemAttributeModifiers.EMPTY, null, null)
            );
      }

      public static void put(ResourceLocation location, ReferenceFields referenceFields) {
            REFERENCES.put(location, referenceFields);
      }

}
