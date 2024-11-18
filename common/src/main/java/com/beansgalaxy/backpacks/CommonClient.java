package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.components.EnderTraits;
import com.beansgalaxy.backpacks.registry.ModItems;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.lunch_box.LunchBoxTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.Tint;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import org.apache.commons.lang3.math.Fraction;

import java.util.Optional;

public class CommonClient {

      public static void init() {

      }

      public static final ItemStack NO_GUI_STAND_IN = new ItemStack(Items.AIR);
      public static final ClampedItemPropertyFunction NO_GUI_PREDICATE = (itemStack, clientLevel, livingEntity, i) -> {
            if (itemStack == NO_GUI_STAND_IN && clientLevel == null && livingEntity == null && i == 0)
                  return 1;

            return 0;
      };

      public static final ClampedItemPropertyFunction EATING_TRAIT_ITEM_PREDICATE = (itemStack, clientLevel, livingEntity, i) ->
                  livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack && LunchBoxTraits.get(itemStack).isPresent()
                              ? 1.0F : 0.0F;

      public static final ClampedItemPropertyFunction FULLNESS_ITEM_PREDICATE = (itemStack, clientLevel, livingEntity, i) -> {
            Optional<GenericTraits> optional = Traits.get(itemStack);
            GenericTraits traits;
            PatchedComponentHolder holder;
            if (optional.isPresent()) {
                  traits = optional.get();
                  holder = PatchedComponentHolder.of(itemStack);
            }
            else {
                  EnderTraits enderTraits = itemStack.get(Traits.ENDER);
                  if (enderTraits == null)
                        return 0f;

                  traits = enderTraits.getTrait(clientLevel);
                  holder = enderTraits;
            }

            if (traits.isFull(holder))
                  return 1f;

            Fraction fullness = traits.fullness(holder);
            if (traits.isEmpty(holder) || fullness.equals(Fraction.ZERO))
                  return 0f;

            float v = fullness.floatValue();
            return v * 0.89f + 0.1f;
      };

      public static final ClampedItemPropertyFunction ENDER_SEARCHING_PREDICATE = (itemStack, clientLevel, livingEntity, i) -> {
            EnderTraits enderTraits = itemStack.get(Traits.ENDER);
            if (enderTraits == null || !enderTraits.isLoaded())
                  return 1;
            return 0;
      };

      public static final ItemColor LEATHER_BACKPACK_ITEM_COLOR = (itemStack, layer) -> switch (layer) {
            case 0, 2 -> componentTint(itemStack, Constants.DEFAULT_LEATHER_COLOR);
            case 4 -> componentHighlight(itemStack, Constants.DEFAULT_LEATHER_COLOR);
            default -> 0xFFFFFFFF;
      };

      public static final ItemColor BUNDLE_ITEM_COLOR = (itemStack, layer) -> layer != 1 ?
                  componentTint(itemStack, 0xFFcd7b46) : 0xFFFFFFFF;

      private static int componentTint(ItemStack itemStack, int rgbBase) {
            DyedItemColor itemColor = itemStack.get(DataComponents.DYED_COLOR);
            if (itemColor != null) {
                  int rgbTint = itemColor.rgb();
                  return smartAverageTint(rgbTint, rgbBase).rgb();
            }
            return rgbBase;
      }

      private static int componentHighlight(ItemStack itemStack, int rgbBase) {
            DyedItemColor itemColor = itemStack.get(DataComponents.DYED_COLOR);
            if (itemColor != null) {
                  rgbBase = itemColor.rgb();
            }
            Tint tint = new Tint(rgbBase);
            double brightness = tint.brightness();
            Tint.HSL hsl = tint.HSL();
            double lum = hsl.getLum();
            hsl.setLum((Math.cbrt(lum + 0.2) + lum) / 2).rotate(5).setSat(Math.sqrt((hsl.getSat() + brightness) / 2));
            return hsl.rgb();
      }

      public static Tint.HSL smartAverageTint(int rgbTint, int rgbBase) {
            Tint tint = new Tint(rgbTint, true);
            Tint base = new Tint(rgbBase);
            tint.modRGB(
                        r -> (r + r + base.getRed()) / 3,
                        g -> (g + g + base.getGreen()) / 3,
                        b -> (b + b + base.getBlue()) / 3
            );
            Tint.HSL tintHsl = tint.HSL();
            tintHsl.modLum(l -> (Math.sqrt(l) + l + l) / 3);
            return tintHsl;
      }
}
