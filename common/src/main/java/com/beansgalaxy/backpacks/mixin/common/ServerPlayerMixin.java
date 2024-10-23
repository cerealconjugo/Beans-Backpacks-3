package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.access.ChestTraitAccess;
import com.beansgalaxy.backpacks.network.clientbound.OpenChestTraits;
import com.beansgalaxy.backpacks.traits.chest.ChestMenu;
import com.beansgalaxy.backpacks.traits.chest.ChestTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements ChestTraitAccess {

      @Shadow protected abstract void initMenu(AbstractContainerMenu pMenu);

      public ServerPlayerMixin(Level pLevel, BlockPos pPos, float pYRot, GameProfile pGameProfile) {
            super(pLevel, pPos, pYRot, pGameProfile);
      }

      @Override
      public void openChestTraits(ChestTraits chestTraits, Slot slot, AbstractContainerMenu menu) {
            ItemStack backpack = slot.getItem();
            containerMenu = new ChestMenu(-2, getInventory(), chestTraits, PatchedComponentHolder.of(backpack), menu);
            initMenu(containerMenu);

            OpenChestTraits.send((ServerPlayer) (Object) this, -2, slot);
      }

}
