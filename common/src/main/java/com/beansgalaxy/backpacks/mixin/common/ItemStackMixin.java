package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.client.CommonAtClient;
import com.beansgalaxy.backpacks.components.EnderTraits;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.components.reference.NonTrait;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import com.beansgalaxy.backpacks.traits.lunch_box.LunchBoxTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
      @Shadow public abstract Item getItem();

      @Unique public final ItemStack instance = (ItemStack) (Object) this;

      @Inject(method = "isStackable", at = @At("HEAD"), cancellable = true)
      private void backpackIsStackable(CallbackInfoReturnable<Boolean> cir) {
            Traits.runIfPresent(instance, traits -> {
                  if (!traits.isStackable(PatchedComponentHolder.of(instance)))
                        cir.setReturnValue(false);
            });
      }

      @Inject(method = "getTooltipLines", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;)V"))
      private void backpackTooltipLines(Item.TooltipContext pTooltipContext, Player pPlayer, TooltipFlag pTooltipFlag, CallbackInfoReturnable<List<Component>> cir, List<Component> list) {
            if (EquipableComponent.get(instance).isPresent())
                  return;

            Traits.runIfPresent(instance, traits -> {
                  traits.client().appendTooltipLines(traits, list);
            });
      }

      @Inject(method = "getTooltipLines", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", ordinal = 3, target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
      private void backpackAdvancedLines(Item.TooltipContext pTooltipContext, Player pPlayer, TooltipFlag pTooltipFlag, CallbackInfoReturnable<List<Component>> cir, List<Component> list) {
            Traits.runIfPresent(instance, traits -> {
                  traits.client().appendAdvancedLines(traits, list);
            });
      }

      @Inject(method = "addAttributeTooltips", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/world/item/ItemStack;forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V"))
      private void backpackAttributeLines(Consumer<Component> pTooltipAdder, Player pPlayer, CallbackInfo ci, ItemAttributeModifiers itemattributemodifiers, EquipmentSlotGroup[] slotGroups, int var5, int var6, EquipmentSlotGroup slotGroup, MutableBoolean mutableboolean) {
            EquipableComponent.get(instance).ifPresent(equipable ->
                  Traits.runIfPresent(instance, traits -> {
                        if (NonTrait.is(traits) || !equipable.slots().test(slotGroup))
                              return;

                        if (mutableboolean.isTrue()) {
                              pTooltipAdder.accept(CommonComponents.EMPTY);
                              pTooltipAdder.accept(Component.translatable("item.modifiers." + slotGroup.getSerializedName()).withStyle(ChatFormatting.GRAY));
                              mutableboolean.setFalse();
                        }

                        traits.client().appendEquipmentLines(traits, pTooltipAdder);
                  })
            );
      }

      @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", at = @At("HEAD"))
      private void appendReferenceAttributesForEachSlot(EquipmentSlot pEquipmentSLot, BiConsumer<Holder<Attribute>, AttributeModifier> pAction, CallbackInfo ci) {
            ReferenceTrait.ifAttributesPresent(instance, modifiers -> {
                  if (!modifiers.modifiers().isEmpty()) {
                        modifiers.forEach(pEquipmentSLot, pAction);
                  }
            });
      }

      @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V", at = @At(value = "HEAD"))
      private void appendReferenceAttributesForEachGroup(EquipmentSlotGroup pSlotGroup, BiConsumer<Holder<Attribute>, AttributeModifier> pAction, CallbackInfo ci) {
            ReferenceTrait.ifAttributesPresent(instance, modifiers -> {
                  if (!modifiers.modifiers().isEmpty()) {
                        modifiers.forEach(pSlotGroup, pAction);
                  }
            });
      }
}
