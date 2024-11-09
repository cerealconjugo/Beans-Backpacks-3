package com.beansgalaxy.backpacks.screen;

import com.beansgalaxy.backpacks.access.EquipmentSlotAccess;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BackSlot extends Slot implements EquipmentSlotAccess {

      public BackSlot(Inventory inv, int slot) {
            super(inv, slot, 59, 62);
      }
      public BackSlot(Inventory inv, int slot, int x, int y) {
            super(inv, slot, x + 32 + 20 + 16, y - 29 - 13);
      }

      @Override
      public boolean mayPlace(ItemStack stack) {
            boolean standardCheck = stack.isEmpty();
            Boolean orElse = EquipableComponent.get(stack).map(equipable ->
                        equipable.slots().test(EquipmentSlot.BODY)
            ).orElse(false);
            return standardCheck || orElse;
      }

      @Override
      public boolean mayPickup(Player player) {
            ItemStack stack = getItem();
            boolean standardCheck = stack.isEmpty();
            boolean equipment = EquipableComponent.testIfPresent(stack, EquipableComponent::traitRemovable);
            boolean emptyTrait = !Traits.testIfPresent(stack, traits -> !traits.isEmpty(stack));
            return standardCheck || equipment || emptyTrait;
      }

      @Override
      public EquipmentSlot getSlot() {
            return EquipmentSlot.BODY;
      }
}
