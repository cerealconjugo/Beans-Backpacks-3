package com.beansgalaxy.backpacks.traits;

import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.TraitTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Consumer;

public interface IClientTraits {

      void renderTooltip(GenericTraits trait, ItemStack itemstack, GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci);

      default void isBarVisible(GenericTraits trait, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            if (!trait.isEmpty())
                  cir.setReturnValue(true);
      }
      void getBarWidth(GenericTraits trait, ItemStack stack, CallbackInfoReturnable<Integer> cir);

      void getBarColor(GenericTraits trait, ItemStack stack, CallbackInfoReturnable<Integer> cir);

      default void appendTooltipLines(GenericTraits traits, List<Component> lines) {
            int size = traits.size();
            lines.add(Component.translatable("traits.beansbackpacks.inventory." + traits.name() + (size == 1 ? ".solo" : ".size"), size).withStyle(ChatFormatting.GOLD));
      }

      default void appendEquipmentLines(GenericTraits traits, Consumer<Component> pTooltipAdder) {
            int size = traits.size();
            pTooltipAdder.accept(Component.translatable("traits.beansbackpacks.equipment." + traits.name() + (size == 1 ? ".solo" : ".size"), size).withStyle(ChatFormatting.GOLD));
      }

      default void appendAdvancedLines(GenericTraits traits, List<Component> list) {
            traits.fields().location().ifPresent(location -> {
                  list.add(Component.translatable("tooltip.beansbackpacks.advanced.reference", location).withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
            });
      }

      default MutableComponent getTooltipTitle(GenericTraits traits) {
            return Component.translatable("traits.beansbackpacks." + traits.name());
      }

      @Nullable
      <T extends GenericTraits> ClientTooltipComponent getTooltipComponent(TraitTooltip<T> tooltip);

      default boolean mouseScrolled(GenericTraits traits, Level level, Slot hoveredSlot, int containerId, int scrolled) {
            return false;
      }
}
