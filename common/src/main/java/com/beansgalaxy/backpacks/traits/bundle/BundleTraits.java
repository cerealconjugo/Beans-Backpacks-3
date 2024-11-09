package com.beansgalaxy.backpacks.traits.bundle;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.SlotSelection;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class BundleTraits extends BundleLikeTraits {
      public static final String NAME = "bundle";

      public BundleTraits(@Nullable ResourceLocation location, ModSound sound, int size) {
            super(location, sound, size, new SlotSelection());
      }

      public BundleTraits(ResourceLocation location, ModSound sound, int size, SlotSelection selection) {
            super(location, sound, size, selection);
      }

      @Override
      public IClientTraits client() {
            return BundleClient.INSTANCE;
      }

      @Override
      public BundleTraits toReference(ResourceLocation location) {
            return new BundleTraits(location, sound(), size());
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public BundleMutable newMutable(PatchedComponentHolder holder) {
            return new BundleMutable(this, holder);
      }

      @Override
      public String toString() {
            return "BundleTraits{" +
                        "size=" + size() +
                        "sound=" + sound() +
                        location().map(
                                    location -> "location=" + location + '}')
                                    .orElse("}");
      }

      @Override
      public TraitComponentKind<? extends GenericTraits> kind() {
            return Traits.BUNDLE;
      }
}
