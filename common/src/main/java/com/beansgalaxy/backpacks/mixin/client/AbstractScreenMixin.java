package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.components.StackableComponent;
import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractScreenMixin<T extends AbstractContainerMenu> extends Screen {

      @Shadow protected Slot hoveredSlot;

      @Shadow @Final protected T menu;

      @Shadow @Nullable protected abstract Slot findSlot(double mouseX, double mouseY);

      @Shadow protected boolean isQuickCrafting;

      @Shadow @Final protected Set<Slot> quickCraftSlots;

      @Shadow protected abstract void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type);

      @Shadow private boolean skipNextRelease;

      @Shadow @Nullable private Slot lastClickSlot;

      @Shadow protected abstract List<Component> getTooltipFromContainerItem(ItemStack pStack);

      protected AbstractScreenMixin(Component pTitle) {
            super(pTitle);
      }

      @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
      protected void renderTooltip(GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci) {
            if (this.hoveredSlot != null) {
                  ItemStack itemstack = this.hoveredSlot.getItem();
                  Traits.get(itemstack).ifPresentOrElse(trait -> {
                        trait.client().renderTooltip(trait, itemstack, PatchedComponentHolder.of(itemstack), gui, mouseX, mouseY, ci);
                  }, () -> EnderTraits.get(itemstack).ifPresent(enderTraits -> enderTraits.getTrait().ifPresent(trait ->
                        trait.client().renderTooltip(trait, itemstack, enderTraits, gui, mouseX, mouseY, ci)
                  )));
            }
      }

      @Inject(method = "renderTooltip", cancellable = true, at = @At(value = "INVOKE",
                  target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V"))
      private void backpacks_renderStackable(GuiGraphics pGuiGraphics, int pX, int pY, CallbackInfo ci, @Local ItemStack itemstack) {
            StackableComponent component = itemstack.get(ITraitData.STACKABLE);
            if (component == null)
                  return;

            int selectedSlot = component.selection.getSelectedSlot(minecraft.player);
            Holder<Item> item = itemstack.getItemHolder();
            ItemStack selectedStack = component.stacks().get(selectedSlot).withItem(item);

            List<Component> lines;
            if (!selectedStack.has(DataComponents.HIDE_TOOLTIP)) {
                  List<Component> tooltip = this.getTooltipFromContainerItem(selectedStack);
                  lines = new ArrayList<>(tooltip);
                  int i = selectedSlot + 1;
                  String s = i + "/" + component.stacks().size();
                  Component literal = Component.literal(s).withStyle(ChatFormatting.GRAY);
                  lines.add(literal);
            }
            else lines = List.of();

            Optional<TooltipComponent> tooltipImage = selectedStack.getTooltipImage();
            pGuiGraphics.renderTooltip(this.font, lines, tooltipImage, pX, pY);
            ci.cancel();
      }

      @Override
      public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
            if (hoveredSlot != null) {
                  ItemStack stack = hoveredSlot.getItem();
                  int containerId = menu.containerId;
                  ClientLevel level = minecraft.level;

                  int scrolled = Mth.floor(pScrollY + 0.5);
                  if (CommonClient.scrollTraits(stack, level, containerId, scrolled, hoveredSlot))
                        return true;
            }
            return super.mouseScrolled(pMouseX, pMouseY, pScrollX, pScrollY);
      }

      @Unique private int backpackDragType = 0;
      @Unique private Slot backpackDraggedSlot = null;
      @Unique private final HashMap<Slot, ItemStack> backpackDraggedSlots = new HashMap<>();

      @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
      public void backpackDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY, CallbackInfoReturnable<Boolean> cir) {
            ItemStack backpack = menu.getCarried();
            Slot slot = this.findSlot(pMouseX, pMouseY);
            if (backpack.isEmpty())
                  return;

            if (slot != lastClickSlot && slot != backpackDraggedSlot) {
                  EnderTraits.get(backpack).ifPresentOrElse(enderTraits -> {
                        GenericTraits trait = enderTraits.getTrait(minecraft.level);
                        if (trait instanceof ItemStorageTraits storageTraits)
                              beans_Backpacks_3$dragTrait(pButton, storageTraits, slot, cir, enderTraits);
                  }, () -> ItemStorageTraits.runIfPresent(backpack, traits ->
                        beans_Backpacks_3$dragTrait(pButton, traits, slot, cir, PatchedComponentHolder.of(backpack))
                  ));
            }
      }

      @Unique
      private void beans_Backpacks_3$dragTrait(int pButton, ItemStorageTraits traits, Slot slot, CallbackInfoReturnable<Boolean> cir, PatchedComponentHolder holder) {
            isQuickCrafting = false;
            skipNextRelease = true;
            if (backpackDraggedSlots.isEmpty()) {
                  backpackDragType = pButton;

                  if (backpackDraggedSlot != null)
                        clickSlot(traits, backpackDraggedSlot, holder);
                  else if (lastClickSlot != null) {
                        backpackDraggedSlot = lastClickSlot;
                        clickSlot(traits, backpackDraggedSlot, holder);
                  }
            }
            else if (backpackDraggedSlot != null)
                  clickSlot(traits, backpackDraggedSlot, holder);

            backpackDraggedSlot = slot;
            cir.setReturnValue(true);
      }

      @Inject(method = "mouseReleased", at = @At("HEAD"))
      public void backpackReleased(double pMouseX, double pMouseY, int pButton, CallbackInfoReturnable<Boolean> cir) {
            if (!backpackDraggedSlots.isEmpty()) {
                  if (backpackDraggedSlot != null) {
                        ItemStack backpack = menu.getCarried();

                        EnderTraits.get(backpack).ifPresentOrElse(enderTraits -> {
                              GenericTraits trait = enderTraits.getTrait(minecraft.level);
                              if (trait instanceof ItemStorageTraits storageTraits)
                                    clickSlot(storageTraits, backpackDraggedSlot, enderTraits);
                        }, () -> ItemStorageTraits.runIfPresent(backpack, traits ->
                                    clickSlot(traits, backpackDraggedSlot, PatchedComponentHolder.of(backpack)))
                        );

                        backpackDraggedSlot = null;
                  }
                  backpackDraggedSlots.clear();
            }
      }

      @Unique
      private void clickSlot(ItemStorageTraits traits, Slot slot, PatchedComponentHolder holder) {
            if (backpackDragType == 0) {
                  ItemStack itemStack = traits.getFirst(holder);
                  if (itemStack != null && !slot.hasItem()) {
                        if (AbstractContainerMenu.canItemQuickReplace(slot, itemStack, true) && slot.mayPlace(itemStack)) {
                              backpackDraggedSlots.put(backpackDraggedSlot, ItemStack.EMPTY);
                              this.slotClicked(slot, slot.index, 1, ClickType.PICKUP);
                        }
                  }
            } else {
                  ItemStack stack = slot.getItem();
                  boolean mayPickup = slot.mayPickup(minecraft.player);
                  boolean hasItem = slot.hasItem();
                  boolean canFit = traits.canItemFit(holder, stack);
                  boolean isFull = traits.isFull(holder);
                  if (mayPickup && hasItem && canFit && !isFull) {
                        backpackDraggedSlots.put(backpackDraggedSlot, stack.copyWithCount(1));
                        this.slotClicked(slot, slot.index, 1, ClickType.PICKUP);
                  }
            }
      }

      @Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
      public void renderBackpackDraggedSlot(GuiGraphics pGuiGraphics, Slot pSlot, CallbackInfo ci) {
            ItemStack pair = findMatchingBackpackDraggedPair(pSlot);
            if (pair != null) {
                  int i = pSlot.x;
                  int j = pSlot.y;
                  boolean isPickup = backpackDragType != 0;
                  if (isPickup) {
                        pGuiGraphics.renderFakeItem(pair, i, j);

                        if (pSlot.getItem().getCount() == 1) {
                              PoseStack pose = pGuiGraphics.pose();
                              pose.pushPose();
                              pose.translate(0, 0, 200);
                              String pText = String.valueOf(1);
                              pGuiGraphics.drawString(font, pText, i + 19 - 2 - font.width(pText), j + 6 + 3, 16777215, true);
                              pose.popPose();
                        }
                  }

                  pGuiGraphics.fill(i, j, i + 16, j + 16, isPickup ? 200 : 0,-2130706433);
            }
      }

      @Unique @Nullable
      private ItemStack findMatchingBackpackDraggedPair(Slot pSlot) {
            for (Map.Entry<Slot, ItemStack> slotPair : backpackDraggedSlots.entrySet()) {
                  if (slotPair.getKey() == pSlot) {
                        return slotPair.getValue();
                  }
            }
            return null;
      }
}
