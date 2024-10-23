package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.components.PlaceableComponent;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.generic.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.lunch_box.LunchBoxTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.traits.Traits;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Item.class)
public class ItemMixin {

      @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
      private void backpackUseOn(UseOnContext ctx, CallbackInfoReturnable<InteractionResult> cir) {
            ItemStack backpack = ctx.getItemInHand();
            PlaceableComponent.get(backpack).ifPresentOrElse(placeable -> {
                  Optional<GenericTraits> traits = Traits.get(backpack);
                  BackpackEntity entity = BackpackEntity.create(ctx, backpack, placeable, traits);
                  if (entity != null) {
                        ModSound modSound = traits.map(GenericTraits::sound).orElse(ModSound.SOFT);
                        modSound.at(entity, ModSound.Type.PLACE);
                        cir.setReturnValue(InteractionResult.SUCCESS);
                  }
            }, () -> Traits.runIfPresent(backpack, traits ->
                        traits.useOn(ctx, backpack, cir))
            );
      }

      @Inject(method = "use", at = @At("HEAD"), cancellable = true)
      private void backpackUseOn(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
            ItemStack backpack = player.getItemInHand(hand);
            Traits.runIfPresent(backpack, traits -> {
                  traits.use(level, player, hand, backpack, cir);
            });
            if (!cir.isCancelled())
                  EquipableComponent.use(player, hand, backpack, cir);
      }

      @Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
      private void stackOnBackpack(ItemStack backpack, ItemStack thatStack, Slot slot, ClickAction click, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {
            Traits.runIfPresent(backpack, traits -> {
                  traits.stackedOnMe(PatchedComponentHolder.of(backpack), thatStack, slot, click, player, access, cir);
            });
      }

      @Inject(method = "overrideStackedOnOther", at = @At("HEAD"), cancellable = true)
      private void backpackOnStack(ItemStack backpack, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir) {
            Traits.runIfPresent(backpack, traits -> {
                  traits.stackedOnOther(PatchedComponentHolder.of(backpack), slot.getItem(), slot, click, player, cir);
            });
      }

      @Inject(method = "inventoryTick", at = @At("HEAD"))
      private void backpackInInventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected, CallbackInfo ci) {
            Traits.runIfPresent(stack, traits -> {
                  traits.inventoryTick(PatchedComponentHolder.of(stack), level, entity, slot, selected);
            });
      }

      @Inject(method = "canFitInsideContainerItems", at = @At("HEAD"), cancellable = true)
      private void backpackFitInsideContainer(CallbackInfoReturnable<Boolean> cir) {

      }

// ===================================================================================================================== CLIENT METHODS

      @Inject(method = "isBarVisible", at = @At("HEAD"), cancellable = true)
      private void backpackBarVisible(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            if (!stack.isDamageableItem()) {
                  Traits.runIfPresent(stack, traits -> {
                        traits.client().isBarVisible(traits, stack, cir);
//                        cir.setReturnValue(!traits.isEmpty());
            });
            }
      }

      @Inject(method = "getBarWidth", at = @At("HEAD"), cancellable = true)
      private void backpackBarWidth(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
            if (!stack.isDamageableItem()) {
                  Traits.runIfPresent(stack, traits -> {
                        traits.client().getBarWidth(traits, stack, cir);
//                        traits.setBarWidth(cir);
                  });
            }
      }

      @Inject(method = "getBarColor", at = @At("HEAD"), cancellable = true)
      private void backpackBarColor(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
            if (!stack.isDamageableItem()) {
                  Traits.runIfPresent(stack, traits -> {
                        traits.client().getBarColor(traits, stack, cir);
//                        traits.setBarColor(cir);
                  });
            }
      }

      // =============================================================================================================== LUNCH BOX TRAITS

      @Inject(method = "finishUsingItem", at = @At("HEAD"), cancellable = true)
      private void finishUsingLunchBox(ItemStack backpack, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
            LunchBoxTraits.ifPresent(backpack, traits -> {
                  traits.finishUsingItem(backpack, level, entity, cir);
            });
      }

      @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
      private void backpackFitInsideContainer(ItemStack backpack, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
            LunchBoxTraits.firstIsPresent(backpack, entity, food -> {
                  cir.setReturnValue(food.getUseDuration(entity));
            });
      }

      @Inject(method = "getUseAnimation", at = @At("HEAD"), cancellable = true)
      private void lunchBoxUseAnimation(ItemStack backpack, CallbackInfoReturnable<UseAnim> cir) {
            LunchBoxTraits.firstIsPresent(backpack, food -> {
                  cir.setReturnValue(food.getUseAnimation());
            });
      }

}
