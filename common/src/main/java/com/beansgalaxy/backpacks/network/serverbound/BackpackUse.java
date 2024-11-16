package com.beansgalaxy.backpacks.network.serverbound;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class BackpackUse implements Packet2S {
      public static final Type<BackpackUse> ID = new Type<>(ResourceLocation.parse(Constants.MOD_ID + ":use_backpack_s"));

      public BackpackUse(RegistryFriendlyByteBuf buf) {
            this();
      }

      private BackpackUse() {
      }

      public static void send() {
            new BackpackUse().send2S();
      }

      @Override
      public Network2S getNetwork() {
            return Network2S.USE_BACKPACK_2S;
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {

      }

      @Override
      public void handle(Player sender) {
            ItemStack backStack = sender.getItemBySlot(EquipmentSlot.BODY);

            Traits.runIfPresent(backStack, traits -> {
                  CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir =
                              new CallbackInfoReturnable<>("backpack_action_use", true, InteractionResultHolder.pass(backStack));
                  traits.use(sender.level(), sender, InteractionHand.MAIN_HAND, PatchedComponentHolder.of(backStack), cir);
            });
      }

      @Override
      public Type<BackpackUse> type() {
            return ID;
      }
}
