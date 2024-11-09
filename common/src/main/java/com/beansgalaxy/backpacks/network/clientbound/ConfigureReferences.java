package com.beansgalaxy.backpacks.network.clientbound;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.components.PlaceableComponent;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.components.reference.ReferenceFields;
import com.beansgalaxy.backpacks.components.reference.ReferenceTraitRegistry;
import com.beansgalaxy.backpacks.network.Network2C;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.HashMap;
import java.util.Map;

public class ConfigureReferences implements Packet2C {
      final Map<ResourceLocation, ReferenceFields> references;

      private ConfigureReferences(Map<ResourceLocation, ReferenceFields> references) {
            this.references = references;
      }

      public ConfigureReferences(RegistryFriendlyByteBuf buf) {
            int size = buf.readInt();
            HashMap<ResourceLocation, ReferenceFields> map = new HashMap<>();
            for (int i = 0; i < size; i++) {
                  ResourceLocation location = ResourceLocation.STREAM_CODEC.decode(buf);

                  TraitComponentKind<? extends GenericTraits> kind = TraitComponentKind.STREAM_CODEC.decode(buf);
                  GenericTraits fields = buf.readJsonWithCodec(kind.codec()).toReference(location);

                  ItemAttributeModifiers modifiers = ItemAttributeModifiers.STREAM_CODEC.decode(buf);

                  boolean isPlaceable = buf.readBoolean();
                  PlaceableComponent placeableComponent = isPlaceable ? PlaceableComponent.STREAM_CODEC.decode(buf) : null;

                  boolean isEquipable = buf.readBoolean();
                  EquipableComponent equipableComponent = isEquipable ? EquipableComponent.STREAM_CODEC.decode(buf) : null;

                  ReferenceFields referenceFields = new ReferenceFields(fields, modifiers, placeableComponent, equipableComponent);
                  map.put(location, referenceFields);
            }

            this.references = map;
      }

      public static void send(ServerPlayer player) {
            new ConfigureReferences(ReferenceTraitRegistry.REFERENCES).send2C(player);
      }

      @Override
      public Network2C getNetwork() {
            return Network2C.CONFIG_REFERENCES_2C;
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {
            int size = references.size();
            buf.writeInt(size);
            references.forEach(((location, reference) -> {
                  ResourceLocation.STREAM_CODEC.encode(buf, location);
                  TraitComponentKind<? extends GenericTraits> kind = reference.traits().kind();
                  TraitComponentKind.STREAM_CODEC.encode(buf, kind);
                  encode(buf, kind.codec(), reference.traits());

                  ItemAttributeModifiers.STREAM_CODEC.encode(buf, reference.modifiers());

                  boolean isPlaceable = reference.placeable() != null;
                  buf.writeBoolean(isPlaceable);
                  if (isPlaceable)
                        PlaceableComponent.STREAM_CODEC.encode(buf, reference.placeable());

                  boolean isEquipment = reference.equipable() != null;
                  buf.writeBoolean(isEquipment);
                  if (isEquipment)
                        EquipableComponent.STREAM_CODEC.encode(buf, reference.equipable());
            }));
      }

      private <T extends GenericTraits> void encode(RegistryFriendlyByteBuf buf, Codec<T> codec, GenericTraits fields) {
            buf.writeJsonWithCodec(codec, (T) fields);
      }

      @Override
      public void handle() {
            ReferenceTraitRegistry.REFERENCES.clear();
            references.forEach(ReferenceTraitRegistry::put);
      }

      public static Type<ConfigureReferences> ID = new Type<>(ResourceLocation.parse(Constants.MOD_ID + ":config_references_c"));

      @Override
      public Type<? extends CustomPacketPayload> type() {
            return ID;
      }
}
