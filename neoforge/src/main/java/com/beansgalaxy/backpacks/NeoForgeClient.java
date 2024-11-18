package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.client.renderer.BackpackModel;
import com.beansgalaxy.backpacks.client.renderer.BackpackRender;
import com.beansgalaxy.backpacks.client.renderer.EntityRender;
import com.beansgalaxy.backpacks.registry.ModItems;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.TraitTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import java.util.HashSet;
import java.util.Map;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class NeoForgeClient {

      public NeoForgeClient(IEventBus eventBus) {
            ItemProperties.registerGeneric(ResourceLocation.withDefaultNamespace("no_gui"), CommonClient.NO_GUI_PREDICATE);
            ItemProperties.registerGeneric(ResourceLocation.withDefaultNamespace("fullness"), CommonClient.FULLNESS_ITEM_PREDICATE);
            ItemProperties.registerGeneric(ResourceLocation.withDefaultNamespace("eating"), CommonClient.EATING_TRAIT_ITEM_PREDICATE);
            ItemProperties.registerGeneric(ResourceLocation.withDefaultNamespace("searching"), CommonClient.ENDER_SEARCHING_PREDICATE);
      }

      @EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
      public static class ModEvents {

            @SubscribeEvent
            public static void registerColorHandlers(final RegisterColorHandlersEvent.Item event) {
                  event.register(CommonClient.LEATHER_BACKPACK_ITEM_COLOR, ModItems.LEATHER_BACKPACK.get());
                  event.register(CommonClient.BUNDLE_ITEM_COLOR, ModItems.BUNDLE.get());
            }

            @SubscribeEvent
            public static void registerTooltips(final RegisterClientTooltipComponentFactoriesEvent event) {
                  event.register(TraitTooltip.class, tooltip -> {
                        GenericTraits traits = tooltip.traits();
                        return traits.client().getTooltipComponent(tooltip.traits(), tooltip.itemStack(), tooltip.holder(), tooltip.title());
                  });
            }

            @SubscribeEvent
            public static void registerLayerDefinitions(final EntityRenderersEvent.RegisterLayerDefinitions event) {
                  event.registerLayerDefinition(BackpackRender.BACKPACK_MODEL, BackpackModel::getTexturedModelData);
            }

            @SubscribeEvent
            public static void registerEntityRenderer(final EntityRenderersEvent.RegisterRenderers event) {
                  event.registerEntityRenderer(CommonClass.BACKPACK_ENTITY.get(), EntityRender::new);
            }

            @SubscribeEvent
            public static void registerEntityRenderer(final ModelEvent.RegisterAdditional event) {
                  ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
                  Map<ResourceLocation, Resource> resourceMap = resourceManager.listResources("models/backpack", (p_251575_) -> {
                        String s = p_251575_.getPath();
                        return s.endsWith(".json");
                  });

                  for(ResourceLocation resourceLocation: resourceMap.keySet()) {
                        ResourceLocation location = resourceLocation.withPath(path -> path.replaceAll("models/", "").replaceAll(".json", ""));
                        ModelResourceLocation model = ModelResourceLocation.standalone(location);
                        event.register(model);
                  }
            }
      }

//      @EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
      public static class GameEvents {

      }
}
