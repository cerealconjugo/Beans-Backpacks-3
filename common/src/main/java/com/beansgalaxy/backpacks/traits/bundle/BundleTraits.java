package com.beansgalaxy.backpacks.traits.bundle;

import com.beansgalaxy.backpacks.components.SlotSelection;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.MutableBundleLike;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;

public class BundleTraits extends BundleLikeTraits {
      public static final String NAME = "bundle";

      public BundleTraits(ModSound sound, int size) {
            super(sound, size);
      }

      @Override
      public BundleClient client() {
            return BundleClient.INSTANCE;
      }

      @Override
      public BundleEntity entity() {
            return BundleEntity.INSTANCE;
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public MutableBundleLike<BundleTraits> mutable(PatchedComponentHolder holder) {
            return new MutableBundleLike<>(this, holder);
      }

      @Override
      public String toString() {
            return "BundleTraits{" +
                        "size=" + size() +
                        ", sound=" + sound() +
                        '}';
      }

      @Override
      public TraitComponentKind<? extends GenericTraits> kind() {
            return Traits.BUNDLE;
      }
}
