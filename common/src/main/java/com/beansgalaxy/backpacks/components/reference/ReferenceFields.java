package com.beansgalaxy.backpacks.components.reference;

import com.beansgalaxy.backpacks.components.PlaceableComponent;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.traits.IDeclaredFields;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public record ReferenceFields(IDeclaredFields fields, ItemAttributeModifiers modifiers,
                              PlaceableComponent placeable, EquipableComponent equipable
) {
      public static StreamCodec<RegistryFriendlyByteBuf, ReferenceFields> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public void encode(RegistryFriendlyByteBuf buf, ReferenceFields reference) {
                  TraitComponentKind<? extends GenericTraits, ? extends IDeclaredFields> kind = reference.fields.kind();
                  TraitComponentKind.STREAM_CODEC.encode(buf, kind);
                  encode(buf, kind.declaredCodec(), reference.fields);

                  ItemAttributeModifiers.STREAM_CODEC.encode(buf, reference.modifiers);

                  boolean isPlaceable = reference.placeable != null;
                  buf.writeBoolean(isPlaceable);
                  if (isPlaceable)
                        PlaceableComponent.STREAM_CODEC.encode(buf, reference.placeable);

                  boolean isEquipment = reference.equipable != null;
                  buf.writeBoolean(isEquipment);
                  if (isEquipment)
                        EquipableComponent.STREAM_CODEC.encode(buf, reference.equipable);
            }

            private <T extends IDeclaredFields> void encode(RegistryFriendlyByteBuf buf, Codec<T> codec, IDeclaredFields fields) {
                  buf.writeJsonWithCodec(codec, (T) fields);
            }

            @Override
            public ReferenceFields decode(RegistryFriendlyByteBuf buf) {
                  TraitComponentKind<? extends GenericTraits, ? extends IDeclaredFields> kind = TraitComponentKind.STREAM_CODEC.decode(buf);
                  IDeclaredFields fields = buf.readJsonWithCodec(kind.declaredCodec());

                  ItemAttributeModifiers modifiers = ItemAttributeModifiers.STREAM_CODEC.decode(buf);

                  boolean isPlaceable = buf.readBoolean();
                  PlaceableComponent placeableComponent = isPlaceable ? PlaceableComponent.STREAM_CODEC.decode(buf) : null;

                  boolean isEquipable = buf.readBoolean();
                  EquipableComponent equipableComponent = isEquipable ? EquipableComponent.STREAM_CODEC.decode(buf) : null;

                  return new ReferenceFields(fields, modifiers, placeableComponent, equipableComponent);
            }
      };
}
