package com.beansgalaxy.backpacks.traits.experience;

import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.generic.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

public class XpTraits implements GenericTraits {
      public static final String NAME = "experience";
      private final XpFields fields;
      private final int points;

      public XpTraits(XpFields fields, int points) {
            this.fields = fields;
            this.points = points;
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public XpFields fields() {
            return fields;
      }

      public int points() {
            return points;
      }

      @Override
      public IClientTraits client() {
            return XpClient.INSTANCE;
      }

      @Override
      public XpTraits toReference(ResourceLocation location) {
            return new XpTraits(fields.toReference(location), points);
      }

      @Override
      public int size() {
            return fields().size();
      }

      @Override
      public Fraction fullness() {
            return Fraction.getFraction(points, fields().points);
      }

      @Override
      public void stackedOnMe(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {

      }

      @Override
      public void stackedOnOther(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir) {

      }

      @Override
      public void use(Level level, Player player, InteractionHand hand, ItemStack backpack, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
            XpMutable mutable = mutable();

            if (isEmpty()) {
                  int totalExperience = player.totalExperience;
                  if (totalExperience == 0)
                        return;

                  int experienceLevel = player.experienceLevel;
                  if (experienceLevel < size()) {
                        mutable.points = totalExperience;
                        player.totalExperience = 0;
                        player.experienceLevel = 0;
                        player.experienceProgress = 0f;
                  }
                  else {
                        mutable.points = fields().points;
                        int max = totalExperience - mutable.points;
                        new XpPackagable(max).applyTo(player);
                  }

            } else {
                  int points = mutable.points;
                  new XpPackagable(points + player.totalExperience).applyTo(player);
                  mutable.points = 0;
            }
            kind().freezeAndCancel(PatchedComponentHolder.of(backpack, player), mutable);
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
      public boolean isEmpty() {
            return points == 0;
      }

      @Override
      public boolean isStackable() {
            return isEmpty();
      }

      @Override
      public XpMutable mutable() {
            return new XpMutable(this);
      }

      public class XpMutable implements MutableTraits {
            private final XpTraits traits;
            private int points;

            public XpMutable(XpTraits traits) {
                  this.traits = traits;
                  this.points = traits.points;
            }

            @Override
            public GenericTraits freeze() {
                  return new XpTraits(traits.fields(), points);
            }

            @Override
            public ItemStack addItem(ItemStack stack, Player player) {
                  return stack;
            }

            @Override
            public ItemStack removeItemNoUpdate(ItemStack carried, Player player) {
                  return carried;
            }

            @Override
            public void dropItems(Entity backpackEntity) {

            }

            @Override
            public InteractionResult interact(BackpackEntity backpackEntity, Player player, InteractionHand hand) {
                  return InteractionResult.PASS;
            }

            @Override
            public void damageTrait(BackpackEntity backpackEntity, int damage, boolean silent) {
                  if (points == 0)
                        return;

                  backpackEntity.breakAmount = 0;
                  Level level = backpackEntity.level();
                  RandomSource random = backpackEntity.getRandom();

                  int xp;
                  double x = backpackEntity.getX();
                  double y = backpackEntity.getEyeY();
                  double z = backpackEntity.getZ();

                  if (points > 55) {
                        xp = 55;
                        points -= 55;
                        int count = random.nextBoolean() ? 3 : 2;
                        for (int i = 1; i < count; i++) {
                              int orbXp = 20;
                              ExperienceOrb orb = new ExperienceOrb(level, x, y, z, orbXp);
                              orb.setDeltaMovement(random.nextDouble() * 0.1 - 0.05, random.nextDouble() * 0.1, random.nextDouble() * 0.1 - 0.05);
                              level.addFreshEntity(orb);
                              xp -= orbXp;
                        }
                  }
                  else {
                        xp = points;
                        points = 0;
                  }

                  ExperienceOrb orb = new ExperienceOrb(level, x, y, z, xp);
                  orb.setDeltaMovement(random.nextDouble() * 0.1, random.nextDouble() * 0.1, random.nextDouble() * 0.1);
                  level.addFreshEntity(orb);
            }

            @Override
            public GenericTraits trait() {
                  return traits;
            }

            @Override
            public void onPlace(BackpackEntity backpackEntity, Player player, ItemStack backpackStack) {
                  int takenXp = Math.min(player.totalExperience, fields.points);
                  int returnedXp = player.totalExperience - takenXp;
                  XpPackagable xp = new XpPackagable(returnedXp);
                  xp.applyTo(player);

                  points = takenXp;
            }

            @Override
            public void onPickup(BackpackEntity backpackEntity, Player player) {
                  int newXp = player.totalExperience + points;
                  XpPackagable xp = new XpPackagable(newXp);
                  xp.applyTo(player);

                  points = 0;
            }
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof XpTraits xpTraits)) return false;
            return points == xpTraits.points && Objects.equals(fields, xpTraits.fields);
      }

      @Override
      public int hashCode() {
            return Objects.hash(fields, points);
      }

      @Override
      public String toString() {
            return "XpTraits{" +
                        "fields=" + fields +
                        ", points=" + points +
                        '}';
      }
}
