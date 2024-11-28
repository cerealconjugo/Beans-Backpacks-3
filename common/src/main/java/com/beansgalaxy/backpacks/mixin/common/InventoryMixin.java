package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.shorthand.Shorthand;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.DataResult;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Mixin(Inventory.class)
public abstract class InventoryMixin implements BackData {
      @Shadow @Final public Player player;
      @Shadow @Final public NonNullList<ItemStack> items;
      @Shadow @Final public NonNullList<ItemStack> armor;
      @Shadow @Final public NonNullList<ItemStack> offhand;
      @Unique @Final public NonNullList<ItemStack> beans_Backpacks_3$body = NonNullList.withSize(1, ItemStack.EMPTY);
      @Shadow @Final private final List<NonNullList<ItemStack>> compartments = ImmutableList.of(items, armor, offhand, beans_Backpacks_3$body);

      @Shadow public abstract boolean add(int $$0, ItemStack $$1);

      @Shadow public int selected;
      @Unique private boolean beans_Backpacks_3$actionKeyIsDown = false;
      @Unique private boolean beans_Backpacks_3$menuKeyIsDown = false;
      @Unique private int beans_Backpacks_3$tinySlot = -1;

      @Override
      public boolean isActionKeyDown() {
            return beans_Backpacks_3$actionKeyIsDown;
      }

      @Override
      public void setActionKey(boolean actionKeyIsDown) {
            this.beans_Backpacks_3$actionKeyIsDown = actionKeyIsDown;
      }

      @Override
      public boolean isMenuKeyDown() {
            return beans_Backpacks_3$menuKeyIsDown;
      }

      @Override
      public void setMenuKey(boolean menuKeyIsDown) {
            this.beans_Backpacks_3$menuKeyIsDown = menuKeyIsDown;
      }

      @Override
      public int getTinySlot() {
            return beans_Backpacks_3$tinySlot;
      }

      @Unique private Shorthand shorthand;

      @Override @Unique
      public Shorthand getShorthand() {
            if (shorthand == null)
                  shorthand = new Shorthand(player);
            return shorthand;
      }

      @Override
      public void setTinySlot(int tinySlot) {
            beans_Backpacks_3$tinySlot = tinySlot;
      }

      public NonNullList<ItemStack> beans_Backpacks_3$getBody() {
            return beans_Backpacks_3$body;
      }

      @Inject(method = "tick", at = @At("TAIL"))
      public void tickCarriedBackpack(CallbackInfo ci)
      {
            ItemStack carried = player.containerMenu.getCarried();
            Level level = player.level();
            Traits.get(carried).ifPresent(traits ->
                        carried.inventoryTick(level, player, -1, false)
            );
            getShorthand().tick(instance);

            for (Slot slot : player.containerMenu.slots) {
                  ItemStack stack = slot.getItem();
                  EnderTraits.get(stack).ifPresent(enderTraits -> {
                        if (!enderTraits.isLoaded())
                              enderTraits.reload(level);

                        if (player instanceof ServerPlayer serverPlayer) {
                              enderTraits.addListener(serverPlayer);
                        }
                  });
            }
      }

      @Unique @Final public Inventory instance = (Inventory) (Object) this;

      @Inject(method = "add(Lnet/minecraft/world/item/ItemStack;)Z", at = @At(value = "HEAD"), cancellable = true)
      public void addToBackpackBeforeInventory(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            if (!stack.isEmpty()) {
                  ItemStorageTraits.runIfEquipped(player, (traits, equipmentSlot) -> {
                        ItemStack backpack = player.getItemBySlot(equipmentSlot);
                        return traits.pickupToBackpack(player, equipmentSlot, instance, backpack, stack, cir);
                  });
            }
      }

      @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("RETURN"), cancellable = true)
      public void addToBackpackAfterInventory(int $$0, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            if (!cir.getReturnValue()) {
                  ItemStorageTraits.runIfEquipped(player, (traits, equipmentSlot) ->
                              traits.overflowFromInventory(equipmentSlot, player, stack, cir)
                  );
            }
      }

      @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
      private void getShorthandDestroySpeed(BlockState blockState, CallbackInfoReturnable<Float> cir) {
            if (selected >= items.size()) {
                  Shorthand shorthand = getShorthand();
                  int slot = shorthand.getSelected(instance);
                  if (slot == -1)
                        return;

                  float destroySpeed = shorthand.getItem(slot).getDestroySpeed(blockState);
                  cir.setReturnValue(destroySpeed);
            }
      }

      @Inject(method = "getSelected", at = @At("HEAD"), cancellable = true)
      private void getShorthandSelected(CallbackInfoReturnable<ItemStack> cir) {
            if (selected >= items.size()) {
                  Shorthand shorthand = getShorthand();
                  int slot = shorthand.getSelected(instance);
                  if (slot == -1)
                        return;

                  ItemStack stack = shorthand.getItem(slot);
                  if (stack.isEmpty())
                        shorthand.resetSelected(instance);
                  else
                        cir.setReturnValue(stack);
            }
      }

      @Inject(method = "replaceWith", at = @At("TAIL"))
      private void backpackReplaceWith(Inventory that, CallbackInfo ci) {
            getShorthand().replaceWith(Shorthand.get(that));
      }

      @Inject(method = "dropAll", at = @At("TAIL"))
      private void shorthandDropAll(CallbackInfo ci) {
            Iterator<ItemStack> iterator = getShorthand().getContent().iterator();
            while (iterator.hasNext()) {
                  ItemStack itemstack = iterator.next();
                  if (!itemstack.isEmpty())
                        player.drop(itemstack, true, false);
                  iterator.remove();
            }
      }

      @Inject(method = "contains(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("TAIL"), cancellable = true)
      private void shorthandContains(ItemStack pStack, CallbackInfoReturnable<Boolean> cir) {
            Iterable<ItemStack> contents = getShorthand().getContent();
            for (ItemStack itemstack : contents) {
                  if (!itemstack.isEmpty() && ItemStack.isSameItemSameComponents(itemstack, pStack)) {
                        cir.setReturnValue(true);
                        return;
                  }
            }
      }

      @Inject(method = "contains(Lnet/minecraft/tags/TagKey;)Z", at = @At("TAIL"), cancellable = true)
      private void shorthandContains(TagKey<Item> pTag, CallbackInfoReturnable<Boolean> cir) {
            Iterable<ItemStack> contents = getShorthand().getContent();
            for (ItemStack itemstack : contents) {
                  if (!itemstack.isEmpty() && itemstack.is(pTag)) {
                        cir.setReturnValue(true);
                        return;
                  }
            }
      }

      @Inject(method = "contains(Ljava/util/function/Predicate;)Z", at = @At("TAIL"), cancellable = true)
      private void shorthandContains(Predicate<ItemStack> pPredicate, CallbackInfoReturnable<Boolean> cir) {
            Iterable<ItemStack> contents = getShorthand().getContent();
            for (ItemStack itemstack : contents) {
                  if (pPredicate.test(itemstack)) {
                        cir.setReturnValue(true);
                        return;
                  }
            }
      }


      @Inject(method = "clearContent", at = @At("TAIL"))
      private void shorthandClearContent(CallbackInfo ci) {
            getShorthand().clearContent();
      }
}
