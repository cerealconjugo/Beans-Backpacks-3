package com.beansgalaxy.backpacks.traits.experience;

import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.generic.MutableTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.math.Fraction;

public class XpMutable implements MutableTraits {
      private final XpTraits traits;
      private final ITraitData<Integer> amount;
      private final PatchedComponentHolder holder;

      public XpMutable(XpTraits traits, PatchedComponentHolder holder) {
            this.traits = traits;
            amount = ITraitData.AMOUNT.get(holder);
            this.holder = holder;
      }

      public void push() {
            amount.push();
            holder.setChanged();
      }

      @Override
      public ModSound sound() {
            return traits.sound();
      }

      @Override
      public Fraction fullness() {
            Integer amount = this.amount.get();
            if (amount == null)
                  return Fraction.ZERO;

            return Fraction.getFraction(amount, traits.points);
      }

      public void fill(Player player) {
            int totalExperience = player.totalExperience;
            int experienceLevel = player.experienceLevel;
            if (experienceLevel < traits.size()) {
                  amount.set(totalExperience);
                  player.totalExperience = 0;
                  player.experienceLevel = 0;
                  player.experienceProgress = 0f;
            }
            else {
                  int points = traits.points;
                  amount.set(points);
                  int max = totalExperience - points;
                  new XpPackagable(max).applyTo(player);
            }
      }

      public void empty(Player player) {
            int points = amount.get();
            new XpPackagable(points + player.totalExperience).applyTo(player);
            amount.set(0);
      }
}
