package com.beansgalaxy.backpacks.trait.battery;

import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.generic.MutableTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class BatteryMutable implements MutableTraits {
      private final BatteryTraits traits;
      public final ITraitData<ItemStack> item;
      private final PatchedComponentHolder holder;
      private SimpleEnergyStorage storage = null;

      BatteryMutable(BatteryTraits traits, PatchedComponentHolder holder) {
            this.traits = traits;
            this.item = ITraitData.SOLO_STACK.get(holder);
            this.holder = holder;
      }

      public void push() {
            item.push();
            holder.setChanged();
      }

      @Override
      public ModSound sound() {
            return traits.sound();
      }

      @Override
      public Fraction fullness() {
            if (storage == null)
                  return Fraction.ZERO;

            return Fraction.getFraction((int) storage.amount, (int) storage.capacity);
      }

      public ItemStack addItem(ItemStack inserted, Player player) {
            ItemStack stack = item.get();
            if (!inserted.isEmpty() && stack.isEmpty()) {
                  item.set(inserted.split(1));
                  return inserted;
            }
            return null;
      }

      public ItemStack removeItem(ItemStack carried, Player player) {
            if (carried.getCount() == 1 || carried.isEmpty()) {
                  ItemStack returnStack = item.get();
                  item.set(carried);
                  return returnStack;
            }
            return carried;
      }

      public ItemStack insert(ItemStack other, Player player) {
            ItemStack stack = addItem(other, player);
            if (stack == null) {
                  stack = removeItem(other, player);
                  sound().atClient(player, ModSound.Type.REMOVE);
            } else {
                  sound().atClient(player, ModSound.Type.INSERT);
            }

            return stack;
      }

      public SimpleEnergyStorage getStorage() {
            if (storage == null) {
                  storage = new SimpleEnergyStorage(traits.size(), traits.speed(), traits.speed()) {
                        @Override protected void onFinalCommit() {
                              if (amount == 0)
                                    holder.remove(ITraitData.LONG);
                              else holder.set(ITraitData.LONG, amount);
                              super.onFinalCommit();
                        }
                  };
                  storage.amount = holder.getOrElse(ITraitData.LONG, () -> 0L);
            }
            return storage;
      }
}
