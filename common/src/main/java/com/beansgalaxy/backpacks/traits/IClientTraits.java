package com.beansgalaxy.backpacks.traits;

import com.beansgalaxy.backpacks.traits.generic.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Consumer;

public interface IClientTraits<T extends GenericTraits> {

      void renderTooltip(T trait, ItemStack itemStack, PatchedComponentHolder holder, GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci);

      default void isBarVisible(T trait, PatchedComponentHolder holder, CallbackInfoReturnable<Boolean> cir) {
            if (!trait.isEmpty(holder))
                  cir.setReturnValue(true);
      }

      void getBarWidth(T trait, PatchedComponentHolder holder, CallbackInfoReturnable<Integer> cir);

      void getBarColor(T trait, PatchedComponentHolder holder, CallbackInfoReturnable<Integer> cir);

      void appendTooltipLines(T traits, List<Component> lines);

      void appendEquipmentLines(T traits, Consumer<Component> pTooltipAdder);

      default void appendAdvancedLines(T traits, List<Component> list) {
            traits.location().ifPresent(location -> {
                  list.add(Component.translatable("tooltip.beansbackpacks.advanced.reference", location).withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
            });
      }

      @Nullable
      ClientTooltipComponent getTooltipComponent(T traits, ItemStack itemStack, PatchedComponentHolder holder, Component title);

      default boolean mouseScrolled(T traits, Level level, Slot hoveredSlot, int containerId, int scrolled) {
            return false;
      }

      default void renderEntityOverlay(Minecraft minecraft, BackpackEntity backpack, T trait, GuiGraphics gui, DeltaTracker tick) {

      }
}
