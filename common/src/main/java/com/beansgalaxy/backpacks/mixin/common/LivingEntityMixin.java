package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.shorthand.Shorthand;
import com.beansgalaxy.backpacks.traits.lunch_box.LunchBoxTraits;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
      @Shadow protected abstract boolean doesEmitEquipEvent(EquipmentSlot pSlot);

      @Shadow public abstract void remove(RemovalReason pReason);

      @Unique public final LivingEntity instance = (LivingEntity) (Object) this;

      public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
            super(pEntityType, pLevel);
      }

      @Inject(method = "getDrinkingSound", at = @At("HEAD"), cancellable = true)
      private void lunchBoxDrinkingSound(ItemStack pStack, CallbackInfoReturnable<SoundEvent> cir) {
            LunchBoxTraits.firstIsPresent(pStack, instance, food -> {
                  cir.setReturnValue(food.getDrinkingSound());
            });
      }

      @Inject(method = "getEatingSound", at = @At("HEAD"), cancellable = true)
      private void lunchBoxEatingSound(ItemStack pStack, CallbackInfoReturnable<SoundEvent> cir) {
            LunchBoxTraits.firstIsPresent(pStack, instance, food -> {
                  cir.setReturnValue(food.getEatingSound());
            });
      }

      @Inject(method = "onEquipItem", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD,
                  at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSilent()Z"))
      private void backpackOnEquip(EquipmentSlot equipmentSlot, ItemStack oldItem, ItemStack newItem, CallbackInfo ci, boolean flag, Equipable equipment) {
            Optional<EquipableComponent> optional = EquipableComponent.get(newItem);
            if (optional.isEmpty()) {
                  if (equipment != null)
                        return;

                  EquipableComponent.get(oldItem).ifPresent(equipable -> {
                        if (this.doesEmitEquipEvent(equipmentSlot)) {
                              this.gameEvent(GameEvent.UNEQUIP);
                              ci.cancel();
                        }
                  });
                  return;
            }

            ci.cancel();
            if (this.doesEmitEquipEvent(equipmentSlot))
                  this.gameEvent(GameEvent.EQUIP);

            EquipableComponent equipable = optional.get();
            if (!isSilent() && equipable.slots().test(equipmentSlot)) {

                  Optional<Holder<SoundEvent>> equipSound;
                  if (instance instanceof Player player) {
                        if (player.isCreative()) return;
                        equipSound = equipable.getEquipSound();
                  }
                  else equipSound = equipable.getUnEquipOrFallback();

                  equipSound.ifPresent(sound -> {
                        level().playSeededSound(null, getX(), getY(), getZ(), sound, getSoundSource(), 1F, 1F, random.nextLong());
                  });
            }


      }

      @Inject(method = "getMainHandItem", cancellable = true, at = @At("HEAD"))
      private void backpackSyncedData(CallbackInfoReturnable<ItemStack> cir) {
            if (instance instanceof Player player) {
                  Shorthand shorthand = Shorthand.get(player);
                  Inventory inventory = player.getInventory();
                  int shorthandSlot = inventory.selected - inventory.items.size() - shorthand.tools.getSize();
                  if (shorthandSlot == shorthand.getSelectedWeapon()) {
                        ItemStack stack = shorthand.weapons.getItem(shorthand.getSelectedWeapon());
                        cir.setReturnValue(stack);
                  }
            }
      }
}
