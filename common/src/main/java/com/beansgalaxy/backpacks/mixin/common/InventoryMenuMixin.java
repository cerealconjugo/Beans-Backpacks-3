package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.screen.BackSlot;
import com.beansgalaxy.backpacks.shorthand.storage.Shorthand;
import com.beansgalaxy.backpacks.shorthand.storage.ShorthandSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends RecipeBookMenu<CraftingInput, CraftingRecipe> {
      public InventoryMenuMixin(MenuType<?> $$0, int $$1) {
            super($$0, $$1);
      }

      @Inject(method = "<init>", at = @At("RETURN"))
      private void createBackSlot(Inventory inv, boolean active, Player owner, CallbackInfo ci) {
            addSlot(new BackSlot(inv, 41));

            Shorthand shorthand = Shorthand.get(owner);

            for (int i = 0; i < 9; i++)
                  this.addSlot(new ShorthandSlot.ToolSlot(shorthand, i));

            for (int i = 0; i < 9; i++)
                  this.addSlot(new ShorthandSlot.WeaponSlot(shorthand, i));
      }
}
