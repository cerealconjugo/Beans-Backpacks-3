package com.beansgalaxy.backpacks.mixin;

import com.beansgalaxy.backpacks.screen.BackSlot;
import com.beansgalaxy.backpacks.shorthand.Shorthand;
import com.beansgalaxy.backpacks.shorthand.ShorthandSlot;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CreativeModeInventoryScreen.class)
public abstract class CreativeInventoryMixin extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
      public CreativeInventoryMixin(CreativeModeInventoryScreen.ItemPickerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
            super(pMenu, pPlayerInventory, pTitle);
      }

      @Inject(method = "hasClickedOutside", cancellable = true, at = @At("HEAD"))
      private void hasClickedShortSlot(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton, CallbackInfoReturnable<Boolean> cir) {
            if (hoveredSlot != null)
                  cir.setReturnValue(false);
      }

      @Inject(method = "selectTab", at = @At(value = "FIELD", shift = At.Shift.BEFORE, ordinal = 0,
                  target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;destroyItemSlot:Lnet/minecraft/world/inventory/Slot;"))
      private void addBackSlot(CreativeModeTab pTab, CallbackInfo ci) {
            AbstractContainerMenu abstractcontainermenu = this.minecraft.player.inventoryMenu;
            for (Slot slot : abstractcontainermenu.slots) {
                  if (slot instanceof BackSlot backSlot) {
                        CreativeModeInventoryScreen.SlotWrapper wrapped = new CreativeModeInventoryScreen.SlotWrapper(backSlot, 41, 127, 20);
                        menu.slots.set(backSlot.index, wrapped);
                  }
                  if (slot instanceof ShorthandSlot.WeaponSlot shortSlot) {
                        CreativeModeInventoryScreen.SlotWrapper wrapped = new CreativeModeInventoryScreen.SlotWrapper(shortSlot, 41, 200, shortSlot.getContainerSlot() * 18);
                        menu.slots.set(shortSlot.index, wrapped);
                  }
                  if (slot instanceof ShorthandSlot.ToolSlot shortSlot) {
                        CreativeModeInventoryScreen.SlotWrapper wrapped = new CreativeModeInventoryScreen.SlotWrapper(shortSlot, 41, 218, shortSlot.getContainerSlot() * 18);
                        menu.slots.set(shortSlot.index, wrapped);
                  }
            }
      }

      @Inject(method = "slotClicked", at = @At(value = "INVOKE",
                  target = "Lnet/minecraft/world/inventory/InventoryMenu;getItems()Lnet/minecraft/core/NonNullList;"))
      private void backpackQuickMoveDestroyItemSlot(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType, CallbackInfo ci) {
            Shorthand.get(minecraft.player).clearContent();
      }
}
