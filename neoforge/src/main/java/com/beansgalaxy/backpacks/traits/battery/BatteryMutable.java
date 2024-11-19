package com.beansgalaxy.backpacks.traits.battery;

import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.generic.MutableTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.EnergyStorage;
import org.apache.commons.lang3.math.Fraction;

public class BatteryMutable implements MutableTraits {
      private final BatteryTraits traits;
      public final ITraitData<ItemStack> item;
      private final PatchedComponentHolder holder;
      private EnergyStorage storage = null;
      private int starting_energy = -1;

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

            return Fraction.getFraction(storage.getEnergyStored(), storage.getMaxEnergyStored());
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

      public boolean energyHasChanged() {
            return storage != null && starting_energy != -1 && storage.getEnergyStored() != starting_energy;
      }

      public EnergyStorage getStorage() {
            if (storage == null) {
                  Integer amount = holder.getOrDefault(ITraitData.AMOUNT, 0);
                  storage = new EnergyStorage(traits.size(), traits.speed(), traits.speed(), amount);
                  starting_energy = amount;
            }
            return storage;
      }
}
