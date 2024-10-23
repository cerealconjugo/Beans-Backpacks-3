package com.beansgalaxy.backpacks.traits.alchemy;

import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.generic.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.SlotSelection;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AlchemyTraits extends BundleLikeTraits {
      public static final String NAME = "alchemy";
      private final AlchemyFields fields;
      private final List<ItemStack> stacks;

      public AlchemyTraits(AlchemyFields fields, List<ItemStack> stacks) {
            super(Fraction.getFraction(stacks.size(), fields.size()));
            this.fields = fields;
            this.stacks = stacks;
      }

      public AlchemyTraits(AlchemyTraits traits, List<ItemStack> stacks) {
            this(traits.fields, traits.slotSelection, stacks);
      }

      public AlchemyTraits(AlchemyFields fields, SlotSelection selection, List<ItemStack> stacks) {
            super(Fraction.getFraction(stacks.size(), fields.size()), selection);
            this.fields = fields;
            this.stacks = stacks;
      }

      @Override
      public List<ItemStack> stacks() {
            return stacks;
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public AlchemyFields fields() {
            return fields;
      }

      @Override
      public IClientTraits client() {
            return AlchemyClient.INSTANCE;
      }

      @Override
      public AlchemyTraits toReference(ResourceLocation location) {
            return new AlchemyTraits(fields.toReference(location), slotSelection, stacks);
      }

      @Override
      public int size() {
            return fields.size();
      }

      @Override
      public Mutable mutable() {
            return new Mutable();
      }

      @Override
      public boolean canItemFit(ItemStack inserted) {
            Item item = inserted.getItem();
            boolean isPotion = item instanceof PotionItem || Items.HONEY_BOTTLE.equals(item) || Items.MILK_BUCKET.equals(item);
            return isPotion && super.canItemFit(inserted);
      }

      @Override
      public void use(Level level, Player player, InteractionHand hand, ItemStack backpack, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
            if (isEmpty())
                  return;

            Mutable mutable = mutable();
            int selectedSlot = getSelectedSlotSafe(player);
            ItemStack selected = mutable.getItemStacks().get(selectedSlot);
            Item item = selected.getItem();

            if (Items.HONEY_BOTTLE.equals(item)) {
                  if (!player.canEat(false))
                        return;

                  FoodProperties foodproperties = selected.get(DataComponents.FOOD);
                  if (foodproperties == null) {
                        player.drop(selected, true);
                        freezeAndCancel(PatchedComponentHolder.of(backpack), mutable);
                        cir.setReturnValue(InteractionResultHolder.sidedSuccess(backpack, level.isClientSide));
                        return;
                  }

                  player.eat(level, selected, foodproperties);
                  player.playSound(SoundEvents.GLASS_BREAK);
                  if (player instanceof ServerPlayer serverplayer) {
                        CriteriaTriggers.CONSUME_ITEM.trigger(serverplayer, selected);
                        serverplayer.awardStat(Stats.ITEM_USED.get(item));
                        player.removeEffect(MobEffects.POISON);
                  }

                  BlockState blockstate = Blocks.HONEY_BLOCK.defaultBlockState();
                  BlockParticleOption particleOption = new BlockParticleOption(ParticleTypes.BLOCK, blockstate);
                  double eyeY = player.getBoundingBox().maxY;
                  double lowY = (player.getY() + eyeY + eyeY) * 0.334;
                  double rad = Math.PI / 180;
                  double yRot = -player.getYHeadRot() * rad;
                  double xRot = -player.getXRot() * rad;
                  double x = player.getX() + Math.sin(yRot) * 0.4 * Math.cos(xRot);
                  double z = player.getZ() + Math.cos(yRot) * 0.4 * Math.cos(xRot);
                  double yO = Math.sin(xRot) * 0.3;

                  for (int j = 0; j < 5; j++) {
                        double random = level.random.nextDouble();
                        double y = Mth.lerp(random * random, lowY, eyeY) + yO;
                        double xySpeed = (random - 0.5);

                        level.addParticle(particleOption, x, y, z, xySpeed, -random * 0.2, xySpeed);
                  }
            }
            else if (Items.MILK_BUCKET.equals(item))
                  useMilkBucketItem(level, player, item, selected);
            else
                  usePotionLikeItem(level, player, selected, item);

            int size = mutable.getItemStacks().size();
            limitSelectedSlot(selectedSlot, size);
            freezeAndCancel(PatchedComponentHolder.of(backpack), mutable);
            cir.setReturnValue(InteractionResultHolder.sidedSuccess(backpack, level.isClientSide));
      }

      private static void useMilkBucketItem(Level level, Player player, Item item, ItemStack selected) {
            item.finishUsingItem(selected, level, player);
            player.addItem(Items.BUCKET.getDefaultInstance());
            player.playSound(SoundEvents.PLAYER_SPLASH_HIGH_SPEED);
            for (int j = 0; j < 4; j++) {
                  double random = level.random.nextDouble();
                  double y = Mth.lerp(Math.sqrt(random), player.getY(), player.getEyeY());
                  double centered = random - 0.5;
                  double xySpeed = centered * .1;
                  double centered1 = level.random.nextDouble() - 0.5;
                  double centered2 = level.random.nextDouble() - 0.5;
                  level.addParticle(ParticleTypes.SNOWFLAKE, player.getX() + centered1, y, player.getZ() + centered2, xySpeed, -random * 0.2, xySpeed);
            }

            ColorParticleOption particleOption = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(100, 0xFFFFFF));
            for (int j = 0; j < 10; j++) {
                  double random = level.random.nextDouble();
                  double y = Mth.lerp(Math.sqrt(random), player.getY(), player.getEyeY());
                  double xySpeed = (random - 0.5) * .1;
                  level.addParticle(particleOption, player.getX(), y, player.getZ(), xySpeed, -random * 0.2, xySpeed);
            }

            ColorParticleOption alphaOption = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(200, 0xFFFFFF));
            for (int j = 0; j < 8; j++) {
                  double random = level.random.nextDouble();
                  double y = Mth.lerp(random, player.getY(), player.getEyeY());
                  double centered = random - 0.5;
                  double xySpeed = centered * 10;
                  level.addParticle(alphaOption, player.getX(), y, player.getZ(), xySpeed, -random * 0.2, xySpeed);

                  double centered1 = level.random.nextDouble() - 0.5;
                  double centered2 = level.random.nextDouble() - 0.5;
                  level.addParticle(ParticleTypes.WHITE_SMOKE, player.getX() + centered1, y, player.getZ() + centered2, 0, -random * 0.2, 0);
            }
      }

      private static void usePotionLikeItem(Level level, Player player, ItemStack selected, Item item) {
            PotionContents potioncontents = selected.get(DataComponents.POTION_CONTENTS);
            if (potioncontents != null) {
                  Optional<Holder<Potion>> potion = potioncontents.potion();
                  Boolean waterLike = potion.map(holder ->
                              Potions.AWKWARD.equals(holder) ||
                                          Potions.MUNDANE.equals(holder) ||
                                          Potions.THICK.equals(holder) ||
                                          Potions.WATER.equals(holder)
                  ).orElse(false);

                  if (waterLike) {
                        player.extinguishFire();
                        player.playSound(SoundEvents.PLAYER_SPLASH);

                        for (int j = 0; j < 4; j++) {
                              double random = level.random.nextDouble();
                              double y = Mth.lerp(Math.sqrt(random), player.getY(), player.getEyeY());
                              double centered = random - 0.5;
                              double xySpeed = centered * .1;
                              double centered1 = level.random.nextDouble() - 0.5;
                              double centered2 = level.random.nextDouble() - 0.5;
                              level.addParticle(ParticleTypes.FALLING_WATER, player.getX() + centered1, y, player.getZ() + centered2, xySpeed, -random * 0.2, xySpeed);
                        }

                        ColorParticleOption particleOption = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(100, -13083194));
                        for (int j = 0; j < 10; j++) {
                              double random = level.random.nextDouble();
                              double y = Mth.lerp(Math.sqrt(random), player.getY(), player.getEyeY());
                              double xySpeed = (random - 0.5) * .1;
                              level.addParticle(particleOption, player.getX(), y, player.getZ(), xySpeed, -random * 0.2, xySpeed);
                              level.addParticle(ParticleTypes.SPLASH, player.getX(), y, player.getZ(), xySpeed, -random * 0.2, xySpeed);
                        }

                        ColorParticleOption alphaOption = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(200, -13083194));
                        for (int j = 0; j < 8; j++) {
                              double random = level.random.nextDouble();
                              double y = Mth.lerp(random * random, player.getY(), player.getEyeY());
                              double centered = random - 0.5;
                              double xySpeed = centered * 10;
                              level.addParticle(alphaOption, player.getX(), y, player.getZ(), xySpeed, -random * 0.2, xySpeed);
                              level.addParticle(ParticleTypes.SPLASH, player.getX(), y, player.getZ(), xySpeed, -random * 0.2, xySpeed);
                        }

                  }
                  else potioncontents.forEachEffect(effect -> {
                        for (int j = 0; j < 20; j++) {
                              double random = level.random.nextDouble();
                              double y = Mth.lerp(random * random, player.getY(), player.getEyeY());
                              double xySpeed = (random - 0.5);
                              level.addParticle(effect.getParticleOptions(), player.getX(), y, player.getZ(), xySpeed, -random * 0.2, xySpeed);
                        }
                  });
            }

            item.finishUsingItem(selected, level, player);
            player.playSound(SoundEvents.GLASS_BREAK);
      }

      public class Mutable extends MutableBundleLike {

            public Mutable() {
                  super(AlchemyTraits.this);
            }

            @Nullable
            public ItemStack addItem(ItemStack inserted, int slot, @Nullable Player player) {
                  if (!canItemFit(inserted))
                        return null;

                  int i = fullness().compareTo(Fraction.ONE);
                  boolean hasSpace = i < 0;
                  if (!hasSpace && !inserted.isStackable())
                        return null;

                  int insertedCount = inserted.getCount();
                  int count = insertedCount;
                  for (ItemStack stored : getItemStacks()) {
                        if (inserted.isEmpty())
                              return ItemStack.EMPTY;

                        if (ItemStack.isSameItemSameComponents(stored, inserted)) {
                              int maxStackSize = stored.getMaxStackSize();
                              int storedCount = stored.getCount();
                              int insert = Math.min(maxStackSize - storedCount, count);
                              stored.grow(insert);
                              inserted.shrink(insert);
                              count -= insert;
                        }
                  }

                  if (!inserted.isEmpty() && getItemStacks().size() < fields.size()) {
                        ItemStack split = inserted.split(count);
                        count = 0;
                        int selectedSlot = getSelectedSlot(player);
                        getItemStacks().add(selectedSlot, split);
                  }

                  return insertedCount == count ? null : inserted;
            }

            @Override
            public AlchemyTraits freeze() {
                  List<ItemStack> stacks = getItemStacks();
                  stacks.removeIf(ItemStack::isEmpty);
                  return new AlchemyTraits(AlchemyTraits.this, stacks);
            }

            @Override
            public void dropItems(Entity backpackEntity) {

            }

            @Override
            public InteractionResult interact(BackpackEntity backpackEntity, Player player, InteractionHand hand) {
                  Level level = backpackEntity.level();
                  List<ItemStack> stacks = getItemStacks();
                  if (stacks.isEmpty())
                        return InteractionResult.FAIL;

                  int i = 0;
                  ItemStack selected = stacks.get(i);
                  Item item = selected.getItem();

                  if (Items.HONEY_BOTTLE.equals(item)) {
                        if (player.canEat(false)) {
                              FoodProperties foodproperties = selected.get(DataComponents.FOOD);
                              if (foodproperties == null) {
                                    player.drop(selected, true);
                                    stacks.remove(i);
                                    return InteractionResult.FAIL;
                              }

                              player.eat(level, selected, foodproperties);
                              player.playSound(SoundEvents.GLASS_BREAK);
                              if (player instanceof ServerPlayer serverplayer) {
                                    CriteriaTriggers.CONSUME_ITEM.trigger(serverplayer, selected);
                                    serverplayer.awardStat(Stats.ITEM_USED.get(item));
                                    player.removeEffect(MobEffects.POISON);
                              }

                              if (selected.isEmpty())
                                    stacks.remove(i);

                              return InteractionResult.SUCCESS;
                        }
                        else {
                              int size = stacks.size();
                              while (Items.HONEY_BOTTLE.equals(item)) {
                                    i++;
                                    if (i == size) {
                                          backpackEntity.wobble = 8;
                                          return InteractionResult.FAIL;
                                    }

                                    selected = stacks.get(i);
                                    item = selected.getItem();
                              }
                        }
                  }
                  else if (Items.MILK_BUCKET.equals(item))
                        useMilkBucketItem(level, player, item, selected);
                  else 
                        usePotionLikeItem(level, player, selected, item);


                  if (selected.isEmpty())
                        stacks.remove(i);

                  return InteractionResult.SUCCESS;
            }

            @Override
            public AlchemyTraits trait() {
                  return AlchemyTraits.this;
            }
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AlchemyTraits that)) return false;
            return Objects.equals(fields, that.fields) && Objects.equals(stacks(), that.stacks());
      }

      @Override
      public int hashCode() {
            return Objects.hash(fields, stacks());
      }

      @Override
      public String toString() {
            return "AlchemyTraits{" +
                        "fields=" + fields +
                        ", stacks=" + stacks() +
                        '}';
      }
}
