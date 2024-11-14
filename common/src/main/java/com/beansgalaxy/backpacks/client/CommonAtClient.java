package com.beansgalaxy.backpacks.client;

import com.beansgalaxy.backpacks.access.MinecraftAccessor;
import com.beansgalaxy.backpacks.data.EnderStorage;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.lunch_box.LunchBoxTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

public class CommonAtClient {

      public static void playSound(SoundEvent soundEvent, float volume, float pitch) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(soundEvent, pitch, volume));
      }

      public static EnderStorage getEnderStorage() {
            MinecraftAccessor instance = (MinecraftAccessor) Minecraft.getInstance();
            return instance.beans_Backpacks_2$getEnder();
      }

      public static Level getLevel() {
            return Minecraft.getInstance().level;
      }

      public static int getInt() {
            return 200;
      }

      public static void LunchBoxTraitsFirstIsPresent(ItemStack lunchBox, Consumer<ItemStack> ifPresent) {
            LunchBoxTraits.ifPresent(lunchBox, traits -> {
                  List<ItemStack> stacks = lunchBox.get(ITraitData.ITEM_STACKS);
                  if (stacks == null || stacks.isEmpty())
                        return;

                  int selectedSlotSafe = traits.getSelectedSlotSafe(PatchedComponentHolder.of(lunchBox), Minecraft.getInstance().player);
                  ifPresent.accept(stacks.get(selectedSlotSafe));

            });
      }

}
