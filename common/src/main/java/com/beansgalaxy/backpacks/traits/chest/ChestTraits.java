package com.beansgalaxy.backpacks.traits.chest;

import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.components.EnderTraits;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

public class ChestTraits extends ItemStorageTraits {
      public static final String NAME = "chest";
      private final ChestFields fields;
      final Int2ObjectArrayMap<ItemStack> stacks;

      public ChestTraits(ChestFields fields, Int2ObjectArrayMap<ItemStack> stacks) {
            this.fields = fields;
            stacks.defaultReturnValue(ItemStack.EMPTY);
            this.stacks = stacks;
      }

      public ChestTraits(ChestFields fields, NonNullList<ItemStack> stacks) {
            this.fields = fields;

            Int2ObjectArrayMap<ItemStack> map = new Int2ObjectArrayMap<>();
            map.defaultReturnValue(ItemStack.EMPTY);

            int i = 0;
            for (ItemStack stack : stacks) {
                  if (!stack.isEmpty()) {
                        map.put(i, stack);
                  }
                  i++;
            }

            this.stacks = map;
      }

      public static Optional<ChestTraits> get(ItemStack backpack) {
            ChestTraits chestTraits = backpack.get(Traits.CHEST);

            if (chestTraits != null)
                  return Optional.of(chestTraits);

            EnderTraits enderTraits = backpack.get(Traits.ENDER);
            if (enderTraits == null)
                  return Optional.empty();

            Optional<GenericTraits> optional = enderTraits.getTrait();
            if (optional.isEmpty())
                  return Optional.empty();

            if (optional.get() instanceof ChestTraits trait)
                  return Optional.of(trait);

            return Optional.empty();
      }

      @Override
      public List<ItemStack> stacks() {
            if (stacks.isEmpty())
                  return List.of();

            ItemStack[] newStacks = stacksToArray();

            return List.of(newStacks);
      }

      private @NotNull ItemStack[] stacksToArray() {
            int size = size();
            ItemStack[] newStacks = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                  ItemStack stack = stacks.get(i);
                  newStacks[i] = stack;
            }
            return newStacks;
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public ChestFields fields() {
            return fields;
      }

      @Override
      public ChestClient client() {
            return ChestClient.INSTANCE;
      }

      @Override
      public ChestTraits toReference(ResourceLocation location) {
            return new ChestTraits(fields.toReference(location), stacks);
      }

      @Override
      public int size() {
            return fields.columns * fields.rows;
      }

      @Override
      public Fraction fullness() {
            int fullSlots = stacks.values().stream().mapToInt(stack -> stack.isEmpty() ? 0 : 1).sum();
            return Fraction.getFraction(fullSlots, size());
      }

      @Override
      public boolean isFull() {
            return stacks.size() == size() && stacks.values().stream().allMatch(stack -> stack.getCount() == stack.getMaxStackSize());
      }

      @Override
      public void stackedOnMe(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {
            boolean menuKeyDown = BackData.get(player).isMenuKeyDown();
            if (menuKeyDown) {
                  if (player.level().isClientSide)
                        client().openTinyMenu(this, slot);

                  cir.setReturnValue(true);
            }
      }

      @Override
      public void stackedOnOther(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir) {

      }

      @Override
      public boolean isEmpty() {
            return stacks.isEmpty();
      }

      @Override
      public Mutable mutable() {
            return new Mutable();
      }

      public void tinyMenuClick(Slot slot, int index, int button, SlotAccess carriedAccess) {
            ItemStack carried = carriedAccess.get();
            ChestTraits.Mutable mutable = mutable();
            ItemStack stack = mutable.getItem(index);

            if (stack.isEmpty() && carried.isEmpty())
                  return;

            if (!stack.isEmpty() && !carried.isEmpty()) {
                  if (ItemStack.isSameItemSameComponents(stack, carried)) {
                        int count = button == 1
                                    ? 1
                                    : carried.getCount();

                        int toAdd = Math.min(stack.getMaxStackSize() - stack.getCount(), count);
                        stack.grow(toAdd);
                        carried.shrink(toAdd);
                  }
                  else {
                        mutable.setItem(index, carried);
                        carriedAccess.set(stack);
                  }
            }
            else if (button == 1) {
                  if (stack.isEmpty()) {
                        ItemStack copy = carried.copyWithCount(1);
                        carried.shrink(1);
                        mutable.setItem(index, copy);
                  }
                  else {
                        int count = Mth.ceil((float) stack.getCount() / 2);
                        ItemStack split = stack.split(count);
                        carriedAccess.set(split);
                  }
            }
            else {
                  mutable.setItem(index, carried);
                  carriedAccess.set(stack);
            }

            ItemStack backpack = slot.getItem();
            freezeAndCancel(PatchedComponentHolder.of(backpack), mutable);
      }

      public class Mutable extends SimpleContainer implements MutableItemStorage {

            public Mutable() {
                  super(ChestTraits.this.stacksToArray());
            }

            @Override
            public int getMaxAmountToAdd(ItemStack stack) {
                  return getItems().stream().mapToInt(stacks ->
                              !stacks.isEmpty() && ItemStack.isSameItemSameComponents(stack, stacks)
                                          ? stacks.getMaxStackSize() - stacks.getCount()
                                          : 0
                  ).sum();
            }

            @Override
            public List<ItemStack> getItemStacks() {
                  return getItems();
            }

            @Override
            public ChestTraits freeze() {
                  return new ChestTraits(ChestTraits.this.fields, getItems());
            }

            @Override @Nullable
            public ItemStack addItem(ItemStack inserted, Player player) {
                  if (!canItemFit(inserted) || inserted.isEmpty())
                        return null;

                  int i = 0;
                  boolean foundEmptySlot = false;
                  NonNullList<ItemStack> items = getItems();
                  while (!inserted.isEmpty() && i < items.size()) {
                        ItemStack stack = items.get(i);
                        if (!foundEmptySlot && stack.isEmpty()) {
                              items.set(i, inserted);
                              foundEmptySlot = true;
                        }
                        else if (ItemStack.isSameItemSameComponents(inserted, stack)) {
                              int toAdd = Math.min(inserted.getCount(), stack.getMaxStackSize() - stack.getCount());
                              stack.grow(toAdd);
                              inserted.shrink(toAdd);
                        }
                        i++;
                  }

                  return ItemStack.EMPTY;
            }

            @Override
            public ItemStack removeItemNoUpdate(ItemStack carried, Player player) {
                  int i = 0;
                  ItemStack returned = ItemStack.EMPTY;
                  NonNullList<ItemStack> items = getItems();
                  while (returned == ItemStack.EMPTY && i < items.size()) {
                        ItemStack stack = items.get(i);
                        if (stack.isEmpty())
                              continue;

                        returned = stack;
                        items.set(i, ItemStack.EMPTY);
                  }

                  return returned;
            }

            @Override
            public void dropItems(Entity backpackEntity) {

            }

            @Override
            public InteractionResult interact(BackpackEntity backpackEntity, Player player, InteractionHand hand) {
                  return InteractionResult.SUCCESS;
            }

            @Override
            public ItemStorageTraits trait() {
                  return ChestTraits.this;
            }
      }
}
