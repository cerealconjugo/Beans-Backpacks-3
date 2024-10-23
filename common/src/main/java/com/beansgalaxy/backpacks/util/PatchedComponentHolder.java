package com.beansgalaxy.backpacks.util;

import com.beansgalaxy.backpacks.traits.IDeclaredFields;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface PatchedComponentHolder {

      <T extends GenericTraits> void set(DataComponentType<? super T> type, T trait);

      <T> T get(DataComponentType<? extends T> type);

      ItemStack getStack();

      static ItemStackComponentHolder of(ItemStack stack) {
            return new ItemStackComponentHolder(stack);
      }

      static StackReturningComponentHolder of(ItemStack stack, Player player) {
            return new StackReturningComponentHolder(stack, player);
      }

      static EntityComponentHolder of(SynchedEntityData entityData, EntityDataAccessor<ItemStack> stackAccess, EntityDataAccessor<GenericTraits> traitsAccess) {
            return new EntityComponentHolder(entityData, stackAccess, traitsAccess);
      }

      class StackReturningComponentHolder implements PatchedComponentHolder {
            private final ItemStack stack;
            private final Player player;

            public StackReturningComponentHolder(ItemStack stack, Player player) {
                  this.stack = stack;
                  this.player = player;
            }

            @Override
            public <T extends GenericTraits> void set(DataComponentType<? super T> type, T trait) {
                  IDeclaredFields fields = trait.fields();
                  int count = stack.getCount();
                  if (count > 1) {
                        ItemStack copy = stack.copyWithCount(1);
                        push(type, trait, fields, copy);
                        player.addItem(copy);
                        stack.shrink(1);
                  }
                  else push(type, trait, fields, stack);
            }

            @Override
            public <T> T get(DataComponentType<? extends T> pComponent) {
                  return stack.get(pComponent);
            }

            @Override
            public ItemStack getStack() {
                  return stack;
            }
      }

      private static <T extends GenericTraits> void push(DataComponentType<? super T> type, T trait, IDeclaredFields fields, ItemStack stack) {
            fields.location().ifPresentOrElse(
                        location -> {
                              ReferenceTrait reference = stack.get(Traits.REFERENCE);
                              ReferenceTrait update = reference.update(location, trait);
                              stack.set(Traits.REFERENCE, update);
                        },
                        () -> stack.set(type, trait)
            );
      }

      class ItemStackComponentHolder implements PatchedComponentHolder {
            private final ItemStack stack;

            private ItemStackComponentHolder(ItemStack stack) {
                  this.stack = stack;
            }

            @Override
            public <T extends GenericTraits> void set(DataComponentType<? super T> type, T trait) {
                  IDeclaredFields fields = trait.fields();
                  push(type, trait, fields, stack);
            }

            @Override
            public <T> T get(DataComponentType<? extends T> type) {
                  return stack.get(type);
            }

            @Override
            public ItemStack getStack() {
                  return stack;
            }
      }

      record EntityComponentHolder(SynchedEntityData entityData,
                                   EntityDataAccessor<ItemStack> stackAccess,
                                   EntityDataAccessor<GenericTraits> traitsAccess)
                  implements PatchedComponentHolder {

            @Override
            public <T extends GenericTraits> void set(DataComponentType<? super T> type, T trait) {
                  ItemStack stack = entityData.get(stackAccess);
                  IDeclaredFields fields = trait.fields();
                  push(type, trait, fields, stack);

                  entityData.set(stackAccess, stack);
                  entityData.set(traitsAccess, trait);
            }

            @Override
            public <T> T get(DataComponentType<? extends T> type) {
                  return entityData.get(stackAccess).get(type);
            }

            @Override
            public ItemStack getStack() {
                  return entityData.get(stackAccess);
            }
      }


}
