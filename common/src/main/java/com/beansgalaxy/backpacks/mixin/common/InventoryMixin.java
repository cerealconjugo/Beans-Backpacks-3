package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.shorthand.storage.Shorthand;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

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

      @Inject(method = "save", at = @At("TAIL"))
      public void writeBackSlot(ListTag tag, CallbackInfoReturnable<ListTag> cir) {
            ItemStack backStack = beans_Backpacks_3$body.getFirst();
            RegistryAccess access = player.registryAccess();
            if (!backStack.isEmpty()) {
                  CompoundTag compoundTag = new CompoundTag();
                  Tag backSlot = backStack.save(access, new CompoundTag());
                  compoundTag.put("BackSlot", backSlot);
                  tag.add(compoundTag);
            }
            CompoundTag compoundTag = new CompoundTag();
            CompoundTag save = getShorthand().save(compoundTag, access);
            tag.add(save);
      }

      @Inject(method = "load", at = @At("TAIL"))
      public void readMixin(ListTag tag, CallbackInfo info) {
            RegistryAccess access = player.registryAccess();
            for (int i = 0; i < tag.size(); ++i) {
                  CompoundTag compoundTag = tag.getCompound(i);
                  CompoundTag backSlot = compoundTag.getCompound("BackSlot");
                  if (!backSlot.isEmpty()) {
                        Optional<ItemStack> itemStack = ItemStack.parse(access, backSlot);
                        itemStack.ifPresent(stack -> beans_Backpacks_3$body.set(0, stack));
                  }
                  if (compoundTag.contains("Shorthand")) {
                        getShorthand().load(compoundTag, access);
                  }
            }

      }

      @Inject(method = "tick", at = @At("TAIL"))
      public void tickCarriedBackpack(CallbackInfo ci)
      {
            ItemStack carried = player.containerMenu.getCarried();
            Traits.get(carried).ifPresent(traits ->
                        carried.inventoryTick(player.level(), player, -1, false)
            );
            getShorthand().tick();
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
}
