package com.beansgalaxy.backpacks.traits.experience;

import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

public class XpTraits extends GenericTraits {
      public static final String NAME = "experience";
      private final int size;
      public final int points;

      public XpTraits(ModSound sound, int size) {
            super(sound);
            this.size = size;
            this.points = XpTraits.pointsFromLevels(size);
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public XpClient client() {
            return XpClient.INSTANCE;
      }

      @Override
      public XpEntity entity() {
            return XpEntity.INSTANCE;
      }

      public int size() {
            return size;
      }

      @Override
      public Fraction fullness(PatchedComponentHolder holder) {
            Integer amount = holder.get(ITraitData.AMOUNT);
            if (amount == null)
                  return Fraction.ZERO;

            return Fraction.getFraction(amount, points);
      }

      @Override
      public void stackedOnMe(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {

      }

      @Override
      public void stackedOnOther(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir) {

      }

      @Override
      public void use(Level level, Player player, InteractionHand hand, PatchedComponentHolder holder, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
            ItemStack backpack = player.getItemInHand(hand);
            XpMutable mutable = mutable(holder);

            if (isEmpty(holder)) {
                  int totalExperience = player.totalExperience;
                  if (totalExperience == 0)
                        return;

                  mutable.fill(player);
                  int count = backpack.getCount();
                  if (count > 1) {
                        ItemStack pStack = backpack.copyWithCount(count - 1);
                        mutable.push();
                        cir.setReturnValue(InteractionResultHolder.success(backpack));
                        player.setItemInHand(hand, pStack);
                        player.addItem(backpack.copyWithCount(1));
                        return;
                  }
            }
            else mutable.empty(player);

            mutable.push();
            cir.setReturnValue(InteractionResultHolder.success(backpack));
      }

      static int pointsFromLevels(int level) {
            return pointsFromLevels(level, 0);
      }

      private static int pointsFromLevels(int level, int endLevels) {
            int xp = 0;
            while (level > endLevels)
            {
                  level--;
                  if (level >= 30)
                        xp += 112 + (level - 30) * 9;
                  else if (level >= 15)
                        xp += 37 + (level - 15) * 5;
                  else
                        xp += 7 + level * 2;
            }
            return xp;
      }

      @Override
      public boolean isEmpty(PatchedComponentHolder holder) {
            return !holder.has(ITraitData.AMOUNT);
      }

      @Override
      public int getAnalogOutput(PatchedComponentHolder holder) {
            Fraction fullness = fullness(holder);
            if (fullness.compareTo(Fraction.ZERO) == 0)
                  return 0;

            Fraction maximum = Fraction.getFraction(Math.min(size(), 15), 1);
            Fraction fraction = fullness.multiplyBy(maximum);
            return fraction.intValue();
      }

      @Override
      public boolean isStackable(PatchedComponentHolder holder) {
            return isEmpty(holder);
      }

      @Override
      public XpMutable mutable(PatchedComponentHolder holder) {
            return new XpMutable(this, holder);
      }

      @Override
      public TraitComponentKind<? extends GenericTraits> kind() {
            return Traits.EXPERIENCE;
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof XpTraits that)) return false;
            return size() == that.size()
                        && Objects.equals(sound(), that.sound());
      }

      @Override
      public int hashCode() {
            return Objects.hash(size(), sound());
      }

      @Override
      public String toString() {
            return "XpTraits{" +
                        "size=" + size() +
                        ", sound=" + sound() +
                        '}';
      }
}
