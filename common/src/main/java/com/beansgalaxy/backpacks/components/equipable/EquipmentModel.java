package com.beansgalaxy.backpacks.components.equipable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record EquipmentModel(HashMap<Attachment, ResourceLocation> attachments, boolean isBuiltInLeatherModel) {

      public enum Attachment {
            HEAD(),
            BODY(),
            BACK(),
            R_ARM(),
            L_ARM(),
            R_LEG(),
            L_LEG(),
      }

      public static final Codec<HashMap<Attachment, ResourceLocation>> ENTRY_CODEC = RecordCodecBuilder.create(in -> in.group(
                              ResourceLocation.CODEC.optionalFieldOf("head").forGetter(equipable -> Optional.ofNullable(equipable.get(Attachment.HEAD))),
                              ResourceLocation.CODEC.optionalFieldOf("body").forGetter(equipable -> Optional.ofNullable(equipable.get(Attachment.BODY))),
                              ResourceLocation.CODEC.optionalFieldOf("back").forGetter(equipable -> Optional.ofNullable(equipable.get(Attachment.BACK))),
                              ResourceLocation.CODEC.optionalFieldOf("leftArm").forGetter(equipable -> Optional.ofNullable(equipable.get(Attachment.L_ARM))),
                              ResourceLocation.CODEC.optionalFieldOf("rightArm").forGetter(equipable -> Optional.ofNullable(equipable.get(Attachment.R_ARM))),
                              ResourceLocation.CODEC.optionalFieldOf("leftLeg").forGetter(equipable -> Optional.ofNullable(equipable.get(Attachment.L_LEG))),
                              ResourceLocation.CODEC.optionalFieldOf("rightLeg").forGetter(equipable -> Optional.ofNullable(equipable.get(Attachment.R_LEG)))
                  ).apply(in, (h, c, b, la, ra, ll, rl) -> {
                        HashMap<Attachment, ResourceLocation> map = new HashMap<>();
                        putIn(map, Attachment.HEAD, h);
                        putIn(map, Attachment.BODY, c);
                        putIn(map, Attachment.BACK, b);
                        putIn(map, Attachment.L_ARM, la);
                        putIn(map, Attachment.R_ARM, ra);
                        putIn(map, Attachment.L_LEG, ll);
                        putIn(map, Attachment.R_LEG, rl);
                        return map;
                  })
      );

      private static <A, B, M extends Map<A, B>> void putIn(M map, A a, Optional<B> b) {
            b.ifPresent(c ->
                        map.put(a, c)
            );
      }

      public static final Codec<EquipmentModel> CODEC = Codec.withAlternative(ENTRY_CODEC.xmap(model -> new EquipmentModel(model, false), EquipmentModel::attachments),
                  Codec.BOOL.xmap(
                              bool -> new EquipmentModel(new HashMap<>(), true),
                              equipmentModel -> equipmentModel.isBuiltInLeatherModel
                  )
      );

      public static final StreamCodec<RegistryFriendlyByteBuf, EquipmentModel> STREAM_CODEC = new StreamCodec<>() {

            @Override
            public void encode(RegistryFriendlyByteBuf buf, EquipmentModel equipmentModel) {
                  buf.writeBoolean(equipmentModel.isBuiltInLeatherModel);
                  if (equipmentModel.isBuiltInLeatherModel)
                        return;

                  buf.writeMap(equipmentModel.attachments, FriendlyByteBuf::writeEnum, FriendlyByteBuf::writeResourceLocation);
            }

            @Override
            public EquipmentModel decode(RegistryFriendlyByteBuf buf) {
                  if (buf.readBoolean())
                        return new EquipmentModel(new HashMap<>(), true);

                  Map<Attachment, ResourceLocation> map = buf.readMap(buf1 -> buf1.readEnum(Attachment.class), FriendlyByteBuf::readResourceLocation);
                  HashMap<Attachment, ResourceLocation> attachments = new HashMap<>(map);
                  return new EquipmentModel(attachments, false);
            }
      };
}
