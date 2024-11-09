package com.beansgalaxy.backpacks.trait.bucket;

import com.beansgalaxy.backpacks.FabricMain;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.*;
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

public class BucketTraits extends GenericTraits {
      public static final String NAME = "bucket";
      private final int size;

      public BucketTraits(ResourceLocation location, ModSound sound, int size) {
            super(location, sound);
            this.size = size;
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public IClientTraits client() {
            return BucketClient.INSTANCE;
      }

      @Override
      public BucketTraits toReference(ResourceLocation location) {
            return new BucketTraits(location, sound(), size);
      }

      public int size() {
            return size;
      }

      @Override
      public Fraction fullness(PatchedComponentHolder holder) {
            Long stacks = holder.get(ITraitData.LONG);
            if (stacks == null)
                  return Fraction.ZERO;

            return Fraction.getFraction((int) (stacks / FluidConstants.BUCKET), size());
      }

      @Override
      public boolean isEmpty(PatchedComponentHolder holder) {
            return !holder.has(ITraitData.LONG) || !holder.has(FabricMain.DATA_FLUID);
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

            PatchedComponentHolder holder = PatchedComponentHolder.of(backpack);
            BucketMutable mutable = newMutable(holder);
            boolean success = !isEmpty(holder) && mutable.transferTo(storage, resource -> {
                  SoundEvent sound = FluidVariantAttributes.getEmptySound(resource);
                  player.level().playSound(player, player.getX(), player.getEyeY(), player.getZ(), sound, SoundSource.PLAYERS, 1, 1);
                  cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide()));
                  mutable.push();
            });

            if (!success) {
                  success = mutable.transferFrom(storage, resource -> {
                        SoundEvent sound = FluidVariantAttributes.getFillSound(resource);
                        player.level().playSound(player, player.getX(), player.getEyeY(), player.getZ(), sound, SoundSource.PLAYERS, 1, 1);
                        cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide()));
                        mutable.push();
                  });
            }

            if (!success) {
                  super.useOn(ctx, backpack, cir);
            }
      }

      @Override
      public void use(Level level, Player player, InteractionHand hand, ItemStack backpack, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
            BucketMutable mutable = newMutable(PatchedComponentHolder.of(backpack));

            if (player.isDiscrete()
                        ? mutable.tryPlace(level, player, backpack) || mutable.tryPickup(level, player, backpack)
                        : mutable.tryPickup(level, player, backpack) || mutable.tryPlace(level, player, backpack)
            ) {
                  player.awardStat(Stats.ITEM_USED.get(backpack.getItem()));
                  cir.setReturnValue(InteractionResultHolder.sidedSuccess(backpack, level.isClientSide()));
                  mutable.push();
                  return;
            }

            cir.setReturnValue(InteractionResultHolder.fail(backpack));

      }

      @Override
      public void stackedOnMe(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {
            BucketMutable mutable = newMutable(backpack);
            if (EquipableComponent.canEquip(backpack, slot)) {
                  ItemStack itemStack = mutable.addItem(other, player);

                  if (itemStack == null)
                        itemStack = mutable.removeItem(other, player);

                  if (itemStack == other) return;
                  access.set(itemStack);
                  mutable.push(cir);
            } else if (ClickAction.SECONDARY.equals(click)) {
                  ItemStack itemStack = mutable.addItem(other, player);
                  if (itemStack == null)
                        itemStack = mutable.removeItem(other, player);

                  if (itemStack == other) return;
                  access.set(itemStack);
                  mutable.push(cir);
            }
      }

      @Override
      public void stackedOnOther(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir) {
            if (ClickAction.SECONDARY.equals(click) && EquipableComponent.get(backpack).isEmpty()) {
                  BucketMutable mutable = newMutable(backpack);
                  ItemStack itemStack = null;
                  if (slot.mayPickup(player))
                        itemStack = mutable.addItem(other, player);

                  if (itemStack == null) {
                        itemStack = mutable.removeItem(other, player);
                        if (!slot.mayPlace(itemStack)) return;
                  }

                  if (itemStack == other) return;
                  slot.set(itemStack);
                  mutable.push(cir);
            }
      }

      @Override
      public BucketMutable newMutable(PatchedComponentHolder holder) {
            return new BucketMutable(this, holder);
      }

      @Override
      public TraitComponentKind<? extends GenericTraits> kind() {
            return Traits.BUCKET;
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BucketTraits that)) return false;
            return size() == that.size() && Objects.equals(sound(), that.sound()) && Objects.equals(location(), that.location());
      }

      @Override
      public int hashCode() {
            return Objects.hash(size(), sound(), location());
      }

      @Override
      public String toString() {
            return "BucketTraits{" +
                        "size=" + size() +
                        "sound=" + sound() +
                        location().map(
                                    location -> "location=" + location + '}')
                                    .orElse("}");
      }
}
