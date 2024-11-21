package com.beansgalaxy.backpacks.shorthand.storage;

import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.data.DataPack;
import com.beansgalaxy.backpacks.traits.Traits;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.Optional;

public abstract class ShorthandSlot extends Slot {
      private final ShortContainer shortContainer;

      public ShorthandSlot(ShortContainer shortContainer, int pSlot, int pX, int pY) {
            super(shortContainer, pSlot, pX, pY);
            this.shortContainer = shortContainer;
      }

      private static boolean stackHasAttribute(ItemStack stack) {
            ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
            if (modifiers == null) {
                  ReferenceTrait reference = stack.get(Traits.REFERENCE);
                  if (reference == null)
                        return false;

                  Optional<ItemAttributeModifiers> optional = reference.getAttributes();
                  if (optional.isEmpty())
                        return false;

                  modifiers = optional.get();
            }

            double compute = modifiers.compute(1.0, EquipmentSlot.MAINHAND);
            return compute != 1.0;
      }

      public static boolean isTool(ItemStack stack) {
            Item item = stack.getItem();
            return item instanceof DiggerItem ||
                        item instanceof ShearsItem ||
                        DataPack.TOOL_ITEM.contains(item);
      }

      @Override
      public boolean isActive() {
            return getContainerSlot() < shortContainer.getContainerSize();
      }

      public static class WeaponSlot extends ShorthandSlot {

            public WeaponSlot(Shorthand shorthand, int slot) {
                  super(shorthand.weapons, slot, getX(slot), getY(slot));
            }

            public static int getX(int slot) {
                  return 152 - (slot * 18);
            }

            public static int getY(int slot) {
                  return 164;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                  if (ShorthandSlot.stackHasAttribute(stack))
                        return true;

                  Item item = stack.getItem();
                  return item instanceof ProjectileWeaponItem
                  || DataPack.WEAPON_ITEM.contains(item);
            }
      }

      public static class ToolSlot extends ShorthandSlot {

            public ToolSlot(Shorthand shorthand, int slot) {
                  super(shorthand.tools, slot, getX(slot), getY(slot));
            }

            public static int getX(int slot) {
                  return 8 + (slot * 18);
            }

            public static int getY(int slot) {
                  return 164;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                  return ShorthandSlot.isTool(stack);
            }
      }
}
