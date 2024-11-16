package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.client.renderer.BackpackModel;
import com.beansgalaxy.backpacks.client.renderer.BackpackRender;
import com.beansgalaxy.backpacks.client.renderer.EntityRender;
import com.beansgalaxy.backpacks.events.*;
import com.beansgalaxy.backpacks.registry.ModItems;
import com.beansgalaxy.backpacks.components.EnderTraits;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.Tint;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import org.apache.commons.lang3.math.Fraction;

import java.util.Optional;

public class FabricClient implements ClientModInitializer {

      @Override
      public void onInitializeClient() {
            NetworkPackages.registerClient();
            LivingEntityFeatureRendererRegistrationCallback.EVENT.register(new AppendModelLayers());
            PreparableModelLoadingPlugin.register(AppendLoadedModels.LOADER, new AppendLoadedModels());
            TooltipComponentCallback.EVENT.register(new TooltipImageEvent());

            ColorProviderRegistry.ITEM.register(((itemStack, layer) -> switch (layer) {
                  case 0, 2 -> componentTint(itemStack, Constants.DEFAULT_LEATHER_COLOR);
                  case 4 -> componentHighlight(itemStack, Constants.DEFAULT_LEATHER_COLOR);
                  default -> 0xFFFFFFFF;
            }), ModItems.LEATHER_BACKPACK.get());

            ColorProviderRegistry.ITEM.register(((itemStack, layer) -> layer != 1 ? componentTint(itemStack, 0xFFcd7b46) : 0xFFFFFFFF), ModItems.BUNDLE.get());
            ItemProperties.registerGeneric(ResourceLocation.withDefaultNamespace("no_gui"), Constants.NO_GUI_PREDICATE);
            ItemProperties.registerGeneric(ResourceLocation.withDefaultNamespace("fullness"), FULLNESS_ITEM_PREDICATE);
            ItemProperties.register(ModItems.ENDER_POUCH.item, ResourceLocation.withDefaultNamespace("searching"), ENDER_SEARCHING_PREDICATE);
            ItemProperties.register(ModItems.LUNCH_BOX.item, ResourceLocation.withDefaultNamespace("eating"), (itemStack, clientLevel, livingEntity, i) ->
                        livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0F : 0.0F
            );
            ItemProperties.register(ModItems.NETHERITE_LUNCH_BOX.item, ResourceLocation.withDefaultNamespace("eating"), (itemStack, clientLevel, livingEntity, i) ->
                        livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0F : 0.0F
            );

            EntityModelLayerRegistry.registerModelLayer(BackpackRender.BACKPACK_MODEL, BackpackModel::getTexturedModelData);
            EntityRendererRegistry.register(CommonClass.BACKPACK_ENTITY, EntityRender::new);
      }

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

      ClampedItemPropertyFunction FULLNESS_ITEM_PREDICATE = (itemStack, clientLevel, livingEntity, i) -> {
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

      ClampedItemPropertyFunction ENDER_SEARCHING_PREDICATE = (itemStack, clientLevel, livingEntity, i) -> {
            EnderTraits enderTraits = itemStack.get(Traits.ENDER);
            if (enderTraits == null || !enderTraits.isLoaded())
                  return 1;
            return 0;
      };

}
