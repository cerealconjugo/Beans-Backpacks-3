package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.access.ViewableAccessor;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Mixin(Allay.class)
public abstract class AllayMixin extends PathfinderMob implements ViewableAccessor {
      @Shadow public abstract boolean isDancing();

      @Shadow protected abstract boolean isDuplicationItem(ItemStack pStack);

      @Shadow protected abstract boolean canDuplicate();

      @Shadow public abstract Brain<Allay> getBrain();

      protected AllayMixin(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
            super(pEntityType, pLevel);
      }

      @Unique private int teleportToCooldown = 0;

      @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/allay/Allay;isPanicking()Z"))
      private void backpacksTick(CallbackInfo ci) {
            if (teleportToCooldown > 0) {
                  teleportToCooldown--;
                  return;
            }

            if (!beans_Backpacks_3$tryTeleportToLikedPlayer())
                  teleportToCooldown = 20;
      }

      @Unique private boolean beans_Backpacks_3$tryTeleportToLikedPlayer() {
            if (getVehicle() != null || isLeashed())
                  return true;

            ItemStack backpack = getItemBySlot(EquipmentSlot.BODY);
            if (backpack.isEmpty())
                  return true;

            Optional<UUID> optional = brain.getMemory(CommonClass.BACKPACK_OWNER_MEMORY.get());
            if (optional.isEmpty()) {
                  return true;
            }

            UUID uuid = optional.get();
            Player player = level().getPlayerByUUID(uuid);
            if (player == null)
                  return true;

            float distance = player.distanceTo(this);
            if (distance < 64)
                  return true;

            float lookRot = player.getYHeadRot();
            Vec3 playPos = player.position();
            Vec3 allayPos = this.position();
            double l = -Math.toDegrees(Math.atan2(allayPos.x - playPos.x, allayPos.z - playPos.z));
            double yaw = Math.abs(l - lookRot) % 360 - 180;
            boolean yawMatches = Math.abs(yaw) > 90;
            if (yawMatches)
                  return true;

            float f1 = -player.getYRot() - 180 * 0.017453292F;
            float f2 = Mth.cos(f1);
            float f3 = Mth.sin(f1);

            int range = 3;
            Vec3 teleportCenter = new Vec3(f3, 0, f2).multiply(range, 1, range);

            int iterations = 3;
            for (int i = 0; i < iterations; i++) {
                  int j = this.random.nextIntBetweenInclusive(-range, range);
                  int k = this.random.nextIntBetweenInclusive(-range, range);
                  Vec3 test = teleportCenter.add(j, 0, k);
                  if (this.level().noCollision(this, this.getBoundingBox().move(test))) {
                        Vec3 finalPos = player.getPosition(1f).add(test);
                        teleportTo(finalPos.x, finalPos.y, finalPos.z);
                        return false;
                  }
            }

            return true;
      }

      @Inject(method = "mobInteract", cancellable = true, at = @At("HEAD"))
      private void backpacksMobInteract(Player player, InteractionHand pHand, CallbackInfoReturnable<InteractionResult> cir) {
            // TODO: write for instant place too !!including body item check!!

            ItemStack inHand = player.getItemInHand(pHand);
            if (isDancing() && isDuplicationItem(inHand) && canDuplicate())
                  return;

            ItemStack bodyArmorItem = getItemBySlot(EquipmentSlot.BODY);
            if (!bodyArmorItem.isEmpty()) {
                  Optional<GenericTraits> traitsOptional = Traits.get(bodyArmorItem);
                  if (!traitsOptional.isEmpty()) {
                        GenericTraits traits = traitsOptional.get();
                        traits.onPlayerInteract(this, player, bodyArmorItem, cir);
                  }
            }

            if (!BackData.get(player).isActionKeyDown())
                  return;

            if (!bodyArmorItem.isEmpty()) {
                  Optional<EquipableComponent> equipableOptional = EquipableComponent.get(bodyArmorItem);
                  if (equipableOptional.isEmpty())
                        return;

                  EquipableComponent equipable = equipableOptional.get();
                  if (!equipable.slots().test(EquipmentSlot.BODY))
                        return;

                  setItemSlot(EquipmentSlot.BODY, ItemStack.EMPTY);
                  player.setItemSlot(EquipmentSlot.BODY, bodyArmorItem);
                  cir.setReturnValue(InteractionResult.SUCCESS);

                  getBrain().eraseMemory(CommonClass.BACKPACK_OWNER_MEMORY.get());
                  return;
            }

            ItemStack itemInHand = getItemInHand(InteractionHand.MAIN_HAND);
            if (!itemInHand.isEmpty() && !player.addItem(itemInHand))
                  return;

            ItemStack backpack = player.getItemBySlot(EquipmentSlot.BODY);
            Optional<GenericTraits> traitsOptional = Traits.get(backpack);
            if (traitsOptional.isEmpty())
                  return;

            Optional<EquipableComponent> equipableOptional = EquipableComponent.get(backpack);
            if (equipableOptional.isEmpty())
                  return;

            EquipableComponent equipable = equipableOptional.get();
            if (!equipable.slots().test(EquipmentSlot.BODY))
                  return;

            setItemSlot(EquipmentSlot.BODY, backpack);
            player.setItemSlot(EquipmentSlot.BODY, ItemStack.EMPTY);
            cir.setReturnValue(InteractionResult.SUCCESS);

            Brain<Allay> brain = getBrain();
            brain.setMemory(CommonClass.BACKPACK_OWNER_MEMORY.get(), player.getUUID());
            brain.setActiveActivityIfPossible(CommonClass.CHESTER_ACTIVITY.get());

      }

      @Inject(method = "hasItemInHand", at = @At("HEAD"), cancellable = true)
      private void backpackHasItemInHand(CallbackInfoReturnable<Boolean> cir) {
            if (!getItemBySlot(EquipmentSlot.BODY).isEmpty())
                  cir.setReturnValue(true);
      }

      @ModifyArgs(method = "<clinit>", at = @At(value = "INVOKE", ordinal = 0,
                  target = "Lcom/google/common/collect/ImmutableList;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;"))
      private static void backpackAllayMemory(Args args) {
            ImmutableList.Builder<MemoryModuleType<?>> builder = ImmutableList.builder();
            builder.add(CommonClass.BACKPACK_OWNER_MEMORY.get());

            int lastArgIndex = args.size() - 1;
            MemoryModuleType<?>[] oldList = args.get(lastArgIndex);
            MemoryModuleType<?>[] cleanList = new MemoryModuleType<?>[oldList.length + 1];

            int i = 0;
            for (MemoryModuleType<?> type : oldList) {
                  cleanList[i] = type;
                  i++;
            }

            cleanList[i] = CommonClass.BACKPACK_OWNER_MEMORY.get();
            args.set(lastArgIndex, cleanList);
      }

// ===================================================================================================================== Viewable

      @Unique private static final EntityDataAccessor<Boolean> IS_OPEN = SynchedEntityData.defineId(Allay.class, EntityDataSerializers.BOOLEAN);
      private final ViewableBackpack viewable = new ViewableBackpack() {
            @Override public void setOpen(boolean isOpen) {
                  AllayMixin.this.getEntityData().set(IS_OPEN, isOpen);
            }

            @Override public boolean isOpen() {
                  return AllayMixin.this.getEntityData().get(IS_OPEN);
            }

            @Override public void playSound(ModSound.Type type) {
                  Traits.get(toStack()).ifPresent(traits -> traits.sound().at(AllayMixin.this, type));
            }

            @Override public int getId() {
                  return AllayMixin.this.getId();
            }

            @Override protected PatchedComponentHolder holder() {
                  return PatchedComponentHolder.of(toStack());
            }

            @Override public ItemStack toStack() {
                  return AllayMixin.this.getItemBySlot(EquipmentSlot.BODY);
            }

            @Override public boolean shouldClose() {
                  if (AllayMixin.this.isRemoved())
                        return true;

                  ItemStack stack = viewable.toStack();
                  if (stack.isEmpty())
                        return true;

                  return Traits.get(stack).isEmpty();
            }
      };

      @Override public ViewableBackpack beans_Backpacks_3$getViewable() {
            return viewable;
      }

      @Inject(method = "defineSynchedData", at = @At("TAIL"))
      private void backpackSyncedData(SynchedEntityData.Builder pBuilder, CallbackInfo ci) {
            pBuilder.define(IS_OPEN, false);
      }
}
