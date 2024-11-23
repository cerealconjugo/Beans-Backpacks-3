package com.beansgalaxy.backpacks.trait.bucket;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.FabricMain;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.generic.MutableTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class BucketMutable extends SingleFluidStorage implements MutableTraits {
      private final BucketTraits traits;
      private final PatchedComponentHolder holder;

      public BucketMutable(BucketTraits traits, PatchedComponentHolder holder) {
            this.traits = traits;
            this.holder = holder;
            amount = holder.getOrElse(ITraitData.LONG, () -> 0L);
            variant = holder.getOrElse(FabricMain.DATA_FLUID, FluidVariant::blank);
      }

      public void push() {
            if (isEmpty()) {
                  holder.remove(ITraitData.LONG);
                  holder.remove(FabricMain.DATA_FLUID);
            } else {
                  holder.set(ITraitData.LONG, amount);
                  holder.set(FabricMain.DATA_FLUID, variant);
            }
            holder.setChanged();
      }

      @Override
      public ModSound sound() {
            return traits.sound();
      }

      @Override
      public Fraction fullness() {
            return Fraction.getFraction((int) (amount / FluidConstants.BUCKET), traits.size());
      }

      @Override
      protected long getCapacity(FluidVariant variant) {
            return FluidConstants.BUCKET * traits.size();
      }

      public boolean isEmpty() {
            return amount == 0 || FluidVariant.blank().equals(variant);
      }

      protected boolean tryPlace(Level level, Player player, ItemStack backpack) {
            BlockHitResult hitResult = Constants.getPlayerPOVHitResult(level, player, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE);
            if (hitResult.getType() == HitResult.Type.MISS || hitResult.getType() != HitResult.Type.BLOCK) {
                  return false;
            }

            BlockPos pos = hitResult.getBlockPos();
            Direction direction = hitResult.getDirection();
            BlockPos relative = pos.relative(direction);
            if (!level.mayInteract(player, pos) || !player.mayUseItemAt(relative, direction, backpack)) {
                  return false;
            }

            if (!this.isEmpty() && this.amount >= FluidConstants.BLOCK) {
                  BlockState blockState = level.getBlockState(pos);
                  BlockPos $$13 = blockState.getBlock() instanceof LiquidBlockContainer && this.variant.isOf(Fluids.WATER) ? pos : relative;
                  if (this.emptyContents(player, level, $$13, hitResult)) {
                        if (player instanceof ServerPlayer) {
                              CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, $$13, backpack);
                        }

                        this.removeAmount(FluidConstants.BLOCK);
                        return true;
                  }
            }

            return false;
      }

      protected boolean tryPickup(Level level, Player player, ItemStack backpack) {
            BlockHitResult hitResult = Constants.getPlayerPOVHitResult(level, player, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY);
            if (hitResult.getType() == HitResult.Type.MISS || hitResult.getType() != HitResult.Type.BLOCK) {
                  return false;
            }

            BlockPos pos = hitResult.getBlockPos();
            Direction direction = hitResult.getDirection();
            BlockPos relative = pos.relative(direction);
            if (!level.mayInteract(player, pos) || !player.mayUseItemAt(relative, direction, backpack)) {
                  return false;
            }

            FluidState fluidState = level.getBlockState(pos).getFluidState();
            BlockState blockState = level.getBlockState(pos);
            Block var14 = blockState.getBlock();
            FluidVariant fluidVariant = FluidVariant.of(fluidState.getType());
            long l = this.getCapacity() - this.amount;
            if (this.canPickupFluidState(fluidState) && !fluidVariant.isBlank() && l >= FluidConstants.BLOCK && var14 instanceof BucketPickup pickup) {
                  ItemStack pickedUpBlock = pickup.pickupBlock(player, level, pos, blockState);
                  if (!pickedUpBlock.isEmpty()) {
                        pickup.getPickupSound().ifPresent(($$1x) -> {
                              player.playSound($$1x, 1.0F, 1.0F);
                        });
                        if (!level.isClientSide) {
                              CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer) player, pickedUpBlock);
                        }

                        level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
                        this.variant = fluidVariant;
                        this.insertAmount(FluidConstants.BLOCK);
                        return true;
                  }
            }
            return false;
      }

      private boolean canPickupFluidState(FluidState fluidState) {
            Fluid type = fluidState.getType();
            return isEmpty() || variant.isOf(type);
      }

      private long removeAmount(long block) {
            long extractedAmount = Math.min(amount, block);

            amount -= extractedAmount;

            if (amount == 0) {
                  variant = getBlankVariant();
            }

            return extractedAmount;
      }

      private long insertAmount(long block) {
            long extractedAmount = Math.min(getCapacity() - amount, block);

            amount += extractedAmount;

            if (amount == 0) {
                  variant = getBlankVariant();
            }

            return extractedAmount;
      }

      private boolean emptyContents(@Nullable Player $$0, Level $$1, BlockPos $$2, @Nullable BlockHitResult $$3) {
            Fluid vanillaFluid = variant.getFluid();
            if (!(vanillaFluid instanceof FlowingFluid $$5)) {
                  return false;
            } else {
                  Block $$7;
                  boolean $$8;
                  LiquidBlockContainer $$15;
                  BlockState $$6;
                  boolean var10000;
                  label82: {
                        $$6 = $$1.getBlockState($$2);
                        $$7 = $$6.getBlock();
                        $$8 = $$6.canBeReplaced(vanillaFluid);
                        if (!$$6.isAir() && !$$8) {
                              label80: {
                                    if ($$7 instanceof LiquidBlockContainer) {
                                          $$15 = (LiquidBlockContainer)$$7;
                                          if ($$15.canPlaceLiquid($$0, $$1, $$2, $$6, vanillaFluid)) {
                                                break label80;
                                          }
                                    }

                                    var10000 = false;
                                    break label82;
                              }
                        }

                        var10000 = true;
                  }

                  boolean $$10 = var10000;
                  if (!$$10) {
                        return $$3 != null && this.emptyContents($$0, $$1, $$3.getBlockPos().relative($$3.getDirection()), null);
                  } else if ($$1.dimensionType().ultraWarm() && vanillaFluid.is(FluidTags.WATER)) {
                        int $$11 = $$2.getX();
                        int $$12 = $$2.getY();
                        int $$13 = $$2.getZ();
                        $$1.playSound($$0, $$2, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + ($$1.random.nextFloat() - $$1.random.nextFloat()) * 0.8F);

                        for(int $$14 = 0; $$14 < 8; ++$$14) {
                              $$1.addParticle(ParticleTypes.LARGE_SMOKE, (double)$$11 + Math.random(), (double)$$12 + Math.random(), (double)$$13 + Math.random(), 0.0, 0.0, 0.0);
                        }

                        return true;
                  } else {
                        if ($$7 instanceof LiquidBlockContainer) {
                              $$15 = (LiquidBlockContainer)$$7;
                              if (variant.isOf(Fluids.WATER)) {
                                    $$15.placeLiquid($$1, $$2, $$6, $$5.getSource(false));
                                    SoundEvent emptySound = FluidVariantAttributes.getEmptySound(variant);
                                    $$1.playSound($$0, $$2, emptySound, SoundSource.BLOCKS, 1.0F, 1.0F);
                                    $$1.gameEvent($$0, GameEvent.FLUID_PLACE, $$2);
                                    return true;
                              }
                        }

                        if (!$$1.isClientSide && $$8 && !$$6.liquid()) {
                              $$1.destroyBlock($$2, true);
                        }

                        if (!$$1.setBlock($$2, vanillaFluid.defaultFluidState().createLegacyBlock(), 11) && !$$6.getFluidState().isSource()) {
                              return false;
                        } else {
                              SoundEvent emptySound = FluidVariantAttributes.getEmptySound(variant);
                              $$1.playSound($$0, $$2, emptySound, SoundSource.BLOCKS, 1.0F, 1.0F);
                              $$1.gameEvent($$0, GameEvent.FLUID_PLACE, $$2);
                              return true;
                        }
                  }
            }
      }

      public boolean transferFrom(Storage<FluidVariant> storage, Consumer<FluidVariant> success) {
            return transfer(storage, this, success);
      }

      public boolean transferTo(Storage<FluidVariant> storage, Consumer<FluidVariant> success) {
            return transfer(this, storage, success);
      }

      private static boolean transfer(Storage<FluidVariant> from, Storage<FluidVariant> to, Consumer<FluidVariant> success) {
            for (StorageView<FluidVariant> view : from) {
                  if (view.isResourceBlank()) continue;
                  FluidVariant resource = view.getResource();

                  long maxExtracted;

                  // check how much can be extracted
                  try (Transaction extractionTestTransaction = Transaction.openOuter()) {
                        maxExtracted = view.extract(resource, Long.MAX_VALUE, extractionTestTransaction);
                        extractionTestTransaction.abort();
                  }

                  try (Transaction transferTransaction = Transaction.openOuter()) {
                        // check how much can be inserted
                        long accepted = to.insert(resource, maxExtracted, transferTransaction);

                        // extract it, or rollback if the amounts don't match
                        if (accepted > 0 && view.extract(resource, accepted, transferTransaction) == accepted) {
                              transferTransaction.commit();
                              success.accept(resource);
                              return true;
                        }
                  }
            }
            return false;
      }

      @Nullable
      public ItemStack addItem(ItemStack stack, Player player) {
            TempSlot slot = new TempSlot(stack);
            Storage<FluidVariant> from = ContainerItemContext.ofPlayerSlot(player, slot).find(FluidStorage.ITEM);

            if (from == null) return null;

            Storage<FluidVariant> to = this;

            if (transfer(from, to, variant -> {
                  SoundEvent sound = FluidVariantAttributes.getEmptySound(variant);
                  if (variant.isOf(Fluids.WATER) && stack.is(Items.POTION))
                        sound = SoundEvents.BOTTLE_EMPTY;

                  player.level().playSound(player, player.getX(), player.getEyeY(), player.getZ(), sound, SoundSource.PLAYERS, 1, 1);
            })) return slot.getStack();

            return null;
      }

      @NotNull
      public ItemStack removeItem(ItemStack carried, Player player) {
            TempSlot slot = new TempSlot(carried);
            Storage<FluidVariant> to = ContainerItemContext.ofPlayerSlot(player, slot).find(FluidStorage.ITEM);
            if (to == null) return carried;

            Storage<FluidVariant> from = this;

            transfer(from, to, variant -> {
                  SoundEvent sound = FluidVariantAttributes.getFillSound(variant);

                  if (variant.isOf(Fluids.WATER) && carried.is(Items.GLASS_BOTTLE))
                        sound = SoundEvents.BOTTLE_FILL;

                  player.level().playSound(player, player.getX(), player.getEyeY(), player.getZ(), sound, SoundSource.PLAYERS, 1, 1);
            });

            return slot.getStack();
      }

      static class TempSlot extends SingleStackStorage {
            private ItemStack stack;

            public TempSlot(ItemStack stack) {
                  this.stack = stack;
            }

            @Override
            public ItemStack getStack() {
                  return stack;
            }

            @Override
            protected void setStack(ItemStack stack) {
                  this.stack = stack;
            }
      }
}
