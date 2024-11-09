package com.beansgalaxy.backpacks.items;

import com.beansgalaxy.backpacks.client.CommonAtClient;
import com.beansgalaxy.backpacks.components.EnderTraits;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class EnderItem extends BackpackItem {
      public EnderItem() {
            super(new Properties().stacksTo(1));
      }

      private static void runIfPresent(ItemStack ender, Player player, CallbackInfoReturnable<Boolean> cir, BiConsumer<EnderTraits, GenericTraits> consumer) {
            getEnderTrait(ender).ifPresent(enderTraits -> {
                  Level level = player.level();
                  GenericTraits traits = enderTraits.getTrait(level);
                  consumer.accept(enderTraits, traits);
                  if (player instanceof ServerPlayer serverPlayer && cir.getReturnValue()) {
                        enderTraits.broadcastChanges(serverPlayer);
                  }
            });
      }

      private static Optional<EnderTraits> getEnderTrait(ItemStack ender) {
            return Optional.ofNullable(ender.get(Traits.ENDER));
      }

      @Override
      public boolean overrideOtherStackedOnMe(ItemStack ender, ItemStack $$1, Slot $$2, ClickAction $$3, Player player, SlotAccess $$5) {
            EnderCallback<Boolean> cir = EnderCallback.of(false);
            runIfPresent(ender, player, cir, (enderTraits, genericTraits) ->
                        genericTraits.stackedOnMe(enderTraits, $$1, $$2, $$3, player, $$5, cir)
            );
            return cir.getReturnValue();
      }

      @Override
      public boolean overrideStackedOnOther(ItemStack ender, Slot slot, ClickAction $$2, Player player) {
            EnderCallback<Boolean> cir = EnderCallback.of(false);
            runIfPresent(ender, player, cir, (enderTraits, genericTraits) ->
                        genericTraits.stackedOnOther(enderTraits, slot.getItem(), slot, $$2, player, cir)
            );
            return cir.getReturnValue();
      }

      @Override
      public void inventoryTick(ItemStack ender, Level level, Entity entity, int slot, boolean selected) {
            getEnderTrait(ender).ifPresent(enderTraits -> {
                  if (!enderTraits.isLoaded())
                        enderTraits.reload(level);

                  enderTraits.getTrait(level).inventoryTick(enderTraits, level, entity, slot, selected);

                  if (entity instanceof ServerPlayer serverPlayer) {
                        enderTraits.addListener(serverPlayer);
                  }
            });
      }

// ==================================================================================================================== CLIENT SYNC ONLY

      @Override
      public void appendHoverText(ItemStack ender, TooltipContext $$1, List<Component> lines, TooltipFlag flag) {
            getEnderTrait(ender).ifPresent(enderTraits -> {
                  GenericTraits trait = enderTraits.getTrait(CommonAtClient.getLevel());
                  Component displayName = enderTraits.getDisplayName();

                  lines.add(Component.translatable("ender.beansbackpacks.bound_player", displayName).withStyle(ChatFormatting.GOLD));
                  trait.client().appendTooltipLines(trait, lines);
                  if (flag.isAdvanced())
                        lines.add(Component.translatable(
                                    "tooltip.beansbackpacks.advanced.reference",
                                    Component.literal(enderTraits.trait().toString())
                        ).withStyle(ChatFormatting.DARK_GRAY));
            });
      }

      @Override
      public boolean isBarVisible(ItemStack ender) {

            EnderTraits enderTraits = ender.get(Traits.ENDER);
            if (enderTraits == null)
                  return false;

            EnderCallback<Boolean> enderCallback = EnderCallback.of(false);
            GenericTraits trait = enderTraits.getTrait(CommonAtClient.getLevel());
            trait.client().isBarVisible(trait, enderTraits, enderCallback);
            return enderCallback.getReturnValue();

      }

      @Override
      public int getBarWidth(ItemStack ender) {
            EnderTraits enderTraits = ender.get(Traits.ENDER);
            if (enderTraits == null)
                  return 13;

            EnderCallback<Integer> cir = EnderCallback.of(13);
            GenericTraits trait = enderTraits.getTrait(CommonAtClient.getLevel());
            trait.client().getBarWidth(trait, enderTraits, cir);
            return cir.getReturnValue();
      }

      @Override
      public int getBarColor(ItemStack ender) {


            EnderTraits enderTraits = ender.get(Traits.ENDER);
            if (enderTraits == null)
                  return 0x000000;

            EnderCallback<Integer> cir = EnderCallback.of(0x000000);
            GenericTraits trait = enderTraits.getTrait(CommonAtClient.getLevel());
            trait.client().getBarColor(trait, enderTraits, cir);

            return cir.getReturnValue();
      }

      public static class EnderCallback<R> extends CallbackInfoReturnable<R> {
            private EnderCallback(R returnValue) {
                  super("ender", true, returnValue);
            }

            static <R> EnderCallback<R> of(R returnValue) {
                  return new EnderCallback<>(returnValue);
            }
      }

}
