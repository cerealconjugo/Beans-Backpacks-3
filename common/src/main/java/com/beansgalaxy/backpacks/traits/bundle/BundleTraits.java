package com.beansgalaxy.backpacks.traits.bundle;

import com.beansgalaxy.backpacks.platform.Services;
import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.util.SlotSelection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class BundleTraits extends BundleLikeTraits {
      public static final String NAME = "bundle";
      private final BundleFields fields;
      private final List<ItemStack> stacks;

      public BundleTraits(BundleFields fields, List<ItemStack> stacks) {
            super(Traits.getWeight(stacks, fields.size()));
            this.fields = fields;
            this.stacks = stacks;
      }

      public BundleTraits(BundleTraits traits, List<ItemStack> stacks) {
            this(traits.fields, traits.slotSelection, stacks);
      }

      public BundleTraits(BundleFields fields, SlotSelection selection, List<ItemStack> stacks) {
            super(Traits.getWeight(stacks, fields.size()), selection);
            this.fields = fields;
            this.stacks = stacks;
      }

      @Override
      public IClientTraits client() {
            return BundleClient.INSTANCE;
      }

      @Override
      public BundleTraits toReference(ResourceLocation location) {
            return new BundleTraits(fields.toReference(location), slotSelection, stacks);
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public BundleFields fields() {
            return fields;
      }

      @Override
      public int size() {
            return fields.size();
      }

      @Override
      public List<ItemStack> stacks() {
            return stacks;
      }

      @Override
      public Mutable mutable() {
            return new Mutable();
      }

      public class Mutable extends MutableBundleLike implements Container {
            public Mutable() {
                  super(BundleTraits.this);
            }

            @Override
            public BundleTraits freeze() {
                  List<ItemStack> stacks = getItemStacks();
                  stacks.removeIf(ItemStack::isEmpty);
                  return new BundleTraits(BundleTraits.this, stacks);
            }

            @Override
            public void dropItems(Entity backpackEntity) {
                  while (!isEmpty()) {
                        ItemStack stack = removeItemNoUpdate(0);
                        backpackEntity.spawnAtLocation(stack);
                  }
            }

            @Override
            public InteractionResult interact(BackpackEntity backpackEntity, Player player, InteractionHand hand) {
                  Services.PLATFORM.openBundleMenu(player, backpackEntity, this);
                  return InteractionResult.SUCCESS;
            }

            @Override
            public BundleTraits trait() {
                  return BundleTraits.this;
            }

            @Override
            public boolean stillValid(@NotNull Player var1) {
                  return true;
            }

            @Override
            public int getContainerSize() {
                  return this.getItemStacks().size();
            }

            @Override @NotNull
            public ItemStack getItem(int slot) {
                  return slot >= this.getItemStacks().size() ? ItemStack.EMPTY : this.getItemStacks().get(slot);
            }

            @Override @NotNull
            public ItemStack removeItem(int slot, int amount) {
                  ItemStack stack = getItem(slot).split(amount);
                  if (stack.isEmpty()) {
                        if (getContainerSize() > slot)
                              this.getItemStacks().remove(slot);
                  }
                  return stack;
            }

            @Override
            public void setItem(int slot, ItemStack stack) {
                  int containerSize = getContainerSize();
                  if (!stack.isEmpty())
                        if (getContainerSize() > slot)
                              getItemStacks().set(slot, stack);
                        else getItemStacks().add(slot, stack);
                  else if (containerSize > slot)
                        getItemStacks().remove(slot);
            }

            @Override
            public void clearContent() {
                  getItemStacks().clear();
            }

            @Override
            public void setChanged() {

            }
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BundleTraits traits)) return false;
            return Objects.equals(fields, traits.fields) && Objects.equals(stacks(), traits.stacks());
      }

      @Override
      public int hashCode() {
            return Objects.hash(fields, stacks());
      }

      @Override
      public String toString() {
            return "BundleTraits{" +
                        "fields=" + fields +
                        ", stacks=" + stacks() +
                        '}';
      }
}
