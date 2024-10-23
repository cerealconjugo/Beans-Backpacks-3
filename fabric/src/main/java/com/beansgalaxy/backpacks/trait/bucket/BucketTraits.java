package com.beansgalaxy.backpacks.trait.bucket;

import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

public class BucketTraits implements GenericTraits {
      public static final String NAME = "bucket";
      private final BucketFields fields;
      protected final FluidVariant fluid;
      protected final long amount;
      private final Fraction weight;

      public BucketTraits(BucketFields fields, FluidVariant fluid, long amount) {
            this(fields, fluid, amount, Fraction.getFraction((int) amount, (int) FluidConstants.BUCKET));
      }

      public BucketTraits(BucketFields fields, FluidVariant fluid, long amount, Fraction weight) {
            this.fields = fields;
            this.fluid = fluid;
            this.amount = amount;
            this.weight = weight;
      }

      public FluidVariant fluid() {
            return fluid;
      }

      public long amount() {
            return amount;
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public BucketFields fields() {
            return fields;
      }

      @Override
      public IClientTraits client() {
            return BucketClient.INSTANCE;
      }

      @Override
      public BucketTraits toReference(ResourceLocation location) {
            return new BucketTraits(fields.toReference(location), fluid, amount);
      }

      @Override
      public int size() {
            return fields().size();
      }

      @Override
      public Fraction fullness() {
            return weight.multiplyBy(Fraction.getFraction(1, size()));
      }

      @Override
      public boolean isEmpty() {
            return amount == 0 || fluid.isBlank();
      }

      @Override
      public void useOn(UseOnContext ctx, ItemStack backpack, CallbackInfoReturnable<InteractionResult> cir) {
            Level level = ctx.getLevel();
            Player player = ctx.getPlayer();
            BlockPos pos = ctx.getClickedPos();
            BlockState state = level.getBlockState(pos);
            Direction direction = ctx.getClickedFace();

            Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos, state, null, direction);
            if (storage == null)
                  return;

            BucketMutable mutable = mutable();
            boolean success = !isEmpty() && mutable.transferTo(storage, resource -> {
                  SoundEvent sound = FluidVariantAttributes.getEmptySound(resource);
                  player.level().playSound(player, player.getX(), player.getEyeY(), player.getZ(), sound, SoundSource.PLAYERS, 1, 1);
                  cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide()));
                  freezeAndCancel(PatchedComponentHolder.of(backpack), mutable);
            });

            if (!success) {
                  success = mutable.transferFrom(storage, resource -> {
                        SoundEvent sound = FluidVariantAttributes.getFillSound(resource);
                        player.level().playSound(player, player.getX(), player.getEyeY(), player.getZ(), sound, SoundSource.PLAYERS, 1, 1);
                        cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide()));
                        freezeAndCancel(PatchedComponentHolder.of(backpack), mutable);
                  });
            }

            if (!success) {
                  GenericTraits.super.useOn(ctx, backpack, cir);
            }
      }

      @Override
      public void use(Level level, Player player, InteractionHand hand, ItemStack backpack, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
            BucketMutable mutable = mutable();

            if (player.isDiscrete()
                        ? mutable.tryPlace(level, player, backpack) || mutable.tryPickup(level, player, backpack)
                        : mutable.tryPickup(level, player, backpack) || mutable.tryPlace(level, player, backpack)
            ) {
                  player.awardStat(Stats.ITEM_USED.get(backpack.getItem()));
                  cir.setReturnValue(InteractionResultHolder.sidedSuccess(backpack, level.isClientSide()));
                  freezeAndCancel(PatchedComponentHolder.of(backpack), mutable);
                  return;
            }

            cir.setReturnValue(InteractionResultHolder.fail(backpack));

      }

      @Override
      public void stackedOnMe(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {
            BucketMutable mutable = mutable();
            if (EquipableComponent.canEquip(backpack, slot)) {
                  ItemStack itemStack = mutable.addItem(other, player);

                  if (itemStack == null)
                        itemStack = mutable.removeItemNoUpdate(other, player);

                  if (itemStack == other) return;
                  access.set(itemStack);
                  freezeAndCancel(backpack, cir, mutable);
            } else if (ClickAction.SECONDARY.equals(click)) {
                  ItemStack itemStack = mutable.addItem(other, player);
                  if (itemStack == null)
                        itemStack = mutable.removeItemNoUpdate(other, player);

                  if (itemStack == other) return;
                  access.set(itemStack);
                  freezeAndCancel(backpack, cir, mutable);
            }
      }

      @Override
      public void stackedOnOther(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir) {
            if (ClickAction.SECONDARY.equals(click) && EquipableComponent.get(backpack).isEmpty()) {
                  BucketMutable mutable = mutable();
                  ItemStack itemStack = null;
                  if (slot.mayPickup(player))
                        itemStack = mutable.addItem(other, player);

                  if (itemStack == null) {
                        itemStack = mutable.removeItemNoUpdate(other, player);
                        if (!slot.mayPlace(itemStack)) return;
                  }

                  if (itemStack == other) return;
                  slot.set(itemStack);
                  freezeAndCancel(backpack, cir, mutable);
            }
      }

      private void freezeAndCancel(PatchedComponentHolder backpack, CallbackInfoReturnable<Boolean> cir, BucketMutable mutable) {
            freezeAndCancel(backpack, mutable);
            cir.setReturnValue(true);
      }

      private void freezeAndCancel(PatchedComponentHolder backpack, BucketMutable mutable) {
            kind().freezeAndCancel(backpack, mutable);
      }

      @Override
      public BucketMutable mutable() {
            return new BucketMutable(this);
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BucketTraits that)) return false;
            return amount == that.amount && Objects.equals(fluid, that.fluid) && Objects.equals(fields, that.fields);
      }

      @Override
      public int hashCode() {
            return Objects.hash(fields, fluid, amount);
      }

      @Override
      public String toString() {
            return "BucketTraits{" +
                        "fields=" + fields +
                        ", fluid=" + fluid +
                        ", amount=" + amount +
                        ", weight=" + weight +
                        '}';
      }
}
