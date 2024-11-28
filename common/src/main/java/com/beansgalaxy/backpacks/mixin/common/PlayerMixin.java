package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.access.ViewableAccessor;
import com.beansgalaxy.backpacks.shorthand.Shorthand;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.quiver.QuiverTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import com.mojang.serialization.DataResult;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Mixin(Player.class)
public abstract class PlayerMixin implements ViewableAccessor {

      @Shadow public abstract ItemStack getItemBySlot(EquipmentSlot pSlot1);

      @Shadow public abstract void setItemSlot(EquipmentSlot pSlot, ItemStack pStack);

      @Unique public final Player instance = (Player) (Object) this;

      @Unique private static final EntityDataAccessor<Boolean> IS_OPEN = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);
      private final ViewableBackpack viewable = new ViewableBackpack() {
            @Override public void setOpen(boolean isOpen) {
                  instance.getEntityData().set(IS_OPEN, isOpen);
            }

            @Override public boolean isOpen() {
                  return instance.getEntityData().get(IS_OPEN);
            }

            @Override public void playSound(ModSound.Type type) {
                  Traits.get(toStack()).ifPresent(traits -> traits.sound().at(instance, type));
            }

            @Override public int getId() {
                  return instance.getId();
            }

            @Override protected PatchedComponentHolder holder() {
                  return PatchedComponentHolder.of(toStack());
            }

            @Override public ItemStack toStack() {
                  return instance.getItemBySlot(EquipmentSlot.BODY);
            }

            Vec3 openedPos = null;
            float openedYaw = 0;

            @Override public void onOpen(Player player) {
                  openedPos = instance.position();
                  openedYaw = instance.yHeadRot;
                  super.onOpen(player);
            }

            @Override public boolean shouldClose() {
                  if (instance.isRemoved())
                        return true;

                  ItemStack stack = viewable.toStack();
                  if (stack.isEmpty())
                        return true;

                  if (Traits.get(stack).isEmpty())
                        return true;

                  if (openedPos == null)
                        return false;

                  if (instance.distanceToSqr(openedPos) > 0.5)
                        return true;

                  double yaw = Math.abs(instance.yHeadRot - openedYaw) % 360 - 180;
                  boolean yawMatches = Math.abs(yaw) > 90;
                  return !yawMatches;
            }
      };

      @Override public ViewableBackpack beans_Backpacks_3$getViewable() {
            return viewable;
      }

      @Inject(method = "getItemBySlot", at = @At("HEAD"), cancellable = true)
      private void getBackSlotItem(EquipmentSlot equipmentSlot, CallbackInfoReturnable<ItemStack> cir) {
            if (equipmentSlot == EquipmentSlot.BODY) {
                  BackData access = (BackData) instance.getInventory();
                  cir.setReturnValue(access.beans_Backpacks_3$getBody().getFirst());
            }
      }

      @Inject(method = "setItemSlot", cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.AFTER,
                  target = "Lnet/minecraft/world/entity/player/Player;verifyEquippedItem(Lnet/minecraft/world/item/ItemStack;)V"))
      private void setBackSlotItem(EquipmentSlot pSlot, ItemStack pStack, CallbackInfo ci) {
            if (EquipmentSlot.BODY.equals(pSlot)) {
                  BackData access = (BackData) instance.getInventory();
                  instance.onEquipItem(EquipmentSlot.BODY, access.beans_Backpacks_3$getBody().set(0, pStack), pStack);
                  ci.cancel();
            }
      }

      @Inject(method = "getProjectile", locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.BEFORE,
                  target = "Lnet/minecraft/world/item/ProjectileWeaponItem;getHeldProjectile(Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Predicate;)Lnet/minecraft/world/item/ItemStack;"))
      private void getBackpackProjectile(ItemStack pShootable, CallbackInfoReturnable<ItemStack> cir, Predicate<ItemStack> predicate) {
            QuiverTraits.runIfQuiverEquipped(instance, (traits, slot, quiver, holder) -> {
                  List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
                  if (stacks == null || stacks.isEmpty())
                        return false;

                  int selectedSlot = traits.getSelectedSlotSafe(holder, instance);
                  ItemStack stack = stacks.get(selectedSlot);
                  if (predicate.test(stack)) {
                        cir.setReturnValue(stack);
                        return true;
                  }

                  return false;
            });
      }

      @Inject(method = "createAttributes", cancellable = true, at = @At(value = "RETURN"))
      private static void addShortAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
            AttributeSupplier.Builder returnValue = cir.getReturnValue();
            AttributeSupplier.Builder add = returnValue.add(CommonClass.TOOL_BELT_ATTRIBUTE, 2).add(CommonClass.SHORTHAND_ATTRIBUTE, 1);
            cir.setReturnValue(add);
      }

       @Inject(method = "interactOn", cancellable = true, at = @At("HEAD"))
      private void backpackInteractOn(Entity pEntityToInteractOn, InteractionHand pHand, CallbackInfoReturnable<InteractionResult> cir) {
             if (pEntityToInteractOn instanceof Player owner) {
                   ItemStack backpack = owner.getItemBySlot(EquipmentSlot.BODY);
                   if (backpack.isEmpty())
                         return;

                   // CHECKS ROTATION OF BOTH PLAYERS
                   double yaw = Math.abs(instance.yHeadRot - owner.yBodyRot) % 360 - 180;
                   boolean yawMatches = Math.abs(yaw) > 90;
                   if (!yawMatches)
                         return;

                   Optional<GenericTraits> optional = Traits.get(backpack);
                   if (optional.isEmpty())
                         return;

                   // OFFSETS OTHER PLAYER'S POSITION
                   double angleRadians = Math.toRadians(owner.yBodyRot);
                   double offset = -0.3;
                   double x = owner.getX();
                   double z = owner.getZ();
                   double offsetX = Math.cos(angleRadians) * offset;
                   double offsetZ = Math.sin(angleRadians) * offset;
                   double newX = x - offsetZ;
                   double newY = owner.getEyeY() - .45;
                   double newZ = z + offsetX;

                   // CHECKS IF PLAYER IS LOOKING
                   Vec3 vec3d = instance.getViewVector(1.0f).normalize();
                   Vec3 vec3d2 = new Vec3(newX - instance.getX(), newY - instance.getEyeY(), newZ - instance.getZ());
                   double d = -vec3d2.length() + 5.65;
                   double e = vec3d.dot(vec3d2.normalize());
                   double maxRadius = 0.05;
                   double radius = (d * d * d * d) / 625;
                   boolean looking = e > 1.0 - radius * maxRadius && instance.hasLineOfSight(owner);
                   if (!looking)
                         return;

                   GenericTraits traits = optional.get();
                   traits.onPlayerInteract(owner, instance, backpack, cir);
             }
      }

      @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
      private void backpackAddSaveData(CompoundTag pCompound, CallbackInfo ci) {
            RegistryAccess access = instance.registryAccess();

            ItemStack backStack = getItemBySlot(EquipmentSlot.BODY);
            CompoundTag backpacks = new CompoundTag();

            RegistryOps<Tag> serializationContext = access.createSerializationContext(NbtOps.INSTANCE);
            DataResult<Tag> dataResult = ItemStack.OPTIONAL_CODEC.encodeStart(serializationContext, backStack);
            dataResult.ifSuccess(back -> backpacks.put("back", back));

            Shorthand shorthand = Shorthand.get(instance);
            shorthand.tools.save(backpacks, access);
            shorthand.weapons.save(backpacks, access);

            pCompound.put(Constants.MOD_ID, backpacks);
      }

      @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
      private void backpackReadSaveData(CompoundTag pCompound, CallbackInfo ci) {
            CompoundTag backpacks = pCompound.getCompound(Constants.MOD_ID);
            RegistryAccess access = instance.registryAccess();

            if (backpacks.contains("back")) {
                  Tag backSlot = backpacks.get("back");
                  RegistryOps<Tag> serializationContext = access.createSerializationContext(NbtOps.INSTANCE);
                  DataResult<ItemStack> back = ItemStack.OPTIONAL_CODEC.parse(serializationContext, backSlot);
                  back.ifSuccess(stack -> setItemSlot(EquipmentSlot.BODY, stack));
            }

            Shorthand shorthand = Shorthand.get(instance);
            shorthand.tools.load(backpacks, access);
            shorthand.weapons.load(backpacks, access);
      }

      @Inject(method = "defineSynchedData", at = @At("TAIL"))
      private void backpackSyncedData(SynchedEntityData.Builder pBuilder, CallbackInfo ci) {
            pBuilder.define(IS_OPEN, false);
      }
}
