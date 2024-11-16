package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.components.PlaceableComponent;
import com.beansgalaxy.backpacks.components.reference.ReferenceRegistry;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.components.reference.NonTrait;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ReloadableServerResources.class)
public class DataResourcesMixin {

      @Inject(method = "loadResources", at = @At("HEAD"))
      private static void catchDataPacks(ResourceManager manager,
                                         LayeredRegistryAccess<RegistryLayer> access,
                                         FeatureFlagSet flagSet,
                                         Commands.CommandSelection commands,
                                         int $$4, Executor $$5, Executor $$6,
                                         CallbackInfoReturnable<CompletableFuture<ReloadableServerResources>> cir)
      {
            ReferenceRegistry.REFERENCES.clear();
            manager.listResources("trait_ids", in -> in.getPath().endsWith(".json"))
                        .forEach(((resourceLocation, resource) -> {
                  try {
                        ResourceLocation location = resourceLocation.withPath(path -> path.replaceFirst(".json", "").replaceFirst("trait_ids/", ""));
                        RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, access.compositeAccess());
                        JsonObject parse = GsonHelper.parse(resource.openAsReader());
                        Iterator<String> iterator = parse.keySet().iterator();

                        PlaceableComponent placeable = null;
                        EquipableComponent equipable = null;
                        ItemAttributeModifiers attributes = ItemAttributeModifiers.EMPTY;
                        GenericTraits fields = NonTrait.INSTANCE;

                        while (iterator.hasNext()) {
                              String type = iterator.next();
                              JsonElement json = parse.get(type);
                              switch (type) {
                                    case "modifiers" -> {
                                          if (ItemAttributeModifiers.EMPTY != attributes)
                                                continue;

                                          DataResult<ItemAttributeModifiers> result = ItemAttributeModifiers.CODEC.parse(registryOps, json);
                                          if (result.isError()) {
                                                String message = "Failure while parsing trait_id \"" + location + "\"; Error while decoding \"" + type + "\"; ";
                                                String error = result.error().get().message();
                                                Constants.LOG.warn("{}{}", message, error);
                                                continue;
                                          }

                                          attributes = result.getOrThrow();
                                    }
                                    case PlaceableComponent.NAME -> {
                                          if (placeable != null)
                                                continue;

                                          DataResult<PlaceableComponent> result = PlaceableComponent.CODEC.parse(registryOps, json);
                                          if (result.isError()) {
                                                String message = "Failure while parsing trait_id \"" + location + "\"; Error while decoding \"" + type + "\"; ";
                                                String error = result.error().get().message();
                                                Constants.LOG.warn("{}{}", message, error);
                                                continue;
                                          }

                                          placeable = result.getOrThrow();
                                    }
                                    case EquipableComponent.NAME -> {
                                          if (equipable != null)
                                                continue;

                                          DataResult<EquipableComponent> result = EquipableComponent.CODEC.parse(registryOps, json);
                                          if (result.isError()) {
                                                String message = "Failure while parsing trait_id \"" + location + "\"; Error while decoding \"" + type + "\"; ";
                                                String error = result.error().get().message();
                                                Constants.LOG.warn("{}{}", message, error);
                                                continue;
                                          }

                                          equipable = result.getOrThrow();
                                    }
                                    case NonTrait.NAME -> {
                                    }
                                    case null -> {
                                    }
                                    default -> {
                                          TraitComponentKind<? extends GenericTraits> kind = TraitComponentKind.get(type);
                                          if (kind == null) {
                                                String message = "Failure while parsing trait_id \"" + location + "\"; The trait \"" + type + "\" does not exist!";
                                                Constants.LOG.warn(message);
                                                continue;
                                          }

                                          DataResult<? extends GenericTraits> result = kind.codec().parse(registryOps, json);
                                          if (result.isError()) {
                                                String message = "Failure while parsing trait_id \"" + location + "\"; Error while decoding \"" + type + "\"; ";
                                                String error = result.error().get().message();
                                                Constants.LOG.warn("{}{}", message, error);
                                                continue;
                                          }

                                          fields = result.getOrThrow().toReference(location);
                                    }
                              }
                        }

                        if (NonTrait.is(fields) && placeable == null && equipable == null)
                              return;

                        ReferenceRegistry.put(location, new ReferenceRegistry(fields, attributes, placeable, equipable));

                  } catch (IOException e) {
                        throw new RuntimeException("error while parsing trait_ids", e);
                  }
            }));
      }

      @Unique
      private static <T extends GenericTraits> void parseAndSaveReference(TraitComponentKind<T> bundle, RegistryOps<JsonElement> registryOps, JsonElement json, ResourceLocation location) {

      }
}
