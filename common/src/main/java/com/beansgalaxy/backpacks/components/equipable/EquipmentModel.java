package com.beansgalaxy.backpacks.components.equipable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class EquipmentModel {
      private final Model model;
      public final boolean useBuiltInLeatherModel;

      public EquipmentModel(Model model, boolean useBuiltInLeatherModel) {
            this.model = model;
            this.useBuiltInLeatherModel = useBuiltInLeatherModel;
      }

      private Model model() {
            return model;
      }
      
      private static <A, B, M extends Map<A, B>> void putIn(M map, A a, Optional<B> b) {
            b.ifPresent(c ->
                        map.put(a, c)
            );
      }

      public void forEach(EquipmentSlot slot, BiConsumer<Attachment, ResourceLocation> consumer) {
            model.forEach((groups, map) -> {
                  if (groups.test(slot))
                        map.forEach(consumer);
            });
      }

      public static class Model extends Object2ObjectArrayMap<EquipmentGroups, HashMap<Attachment, ResourceLocation>> {
            
            public Model() {
                  defaultReturnValue(new HashMap<>());
            }
            
            public static final Codec<HashMap<Attachment, ResourceLocation>> ENTRY_CODEC = RecordCodecBuilder.create(in -> in.group(
                                    ResourceLocation.CODEC.optionalFieldOf("head").forGetter(equipable -> Optional.ofNullable(equipable.get(Attachment.HEAD))),
                                    ResourceLocation.CODEC.optionalFieldOf("body").forGetter(equipable -> Optional.ofNullable(equipable.get(Attachment.BODY))),
                                    ResourceLocation.CODEC.optionalFieldOf("leftArm").forGetter(equipable -> Optional.ofNullable(equipable.get(Attachment.L_ARM))),
                                    ResourceLocation.CODEC.optionalFieldOf("rightArm").forGetter(equipable -> Optional.ofNullable(equipable.get(Attachment.R_ARM))),
                                    ResourceLocation.CODEC.optionalFieldOf("leftLeg").forGetter(equipable -> Optional.ofNullable(equipable.get(Attachment.L_LEG))),
                                    ResourceLocation.CODEC.optionalFieldOf("rightLeg").forGetter(equipable -> Optional.ofNullable(equipable.get(Attachment.R_LEG)))
                        ).apply(in, (h, b, la, ra, ll, rl) -> {
                              HashMap<Attachment, ResourceLocation> map = new HashMap<>();
                              putIn(map, Attachment.HEAD, h);
                              putIn(map, Attachment.BODY, b);
                              putIn(map, Attachment.L_ARM, la);
                              putIn(map, Attachment.R_ARM, ra);
                              putIn(map, Attachment.L_LEG, ll);
                              putIn(map, Attachment.R_LEG, rl);
                              return map;
                        })
            );

            public static final Codec<Model> CODEC = RecordCodecBuilder.create(in ->
                        in.group(
                                    ENTRY_CODEC.optionalFieldOf("any").forGetter(model -> Optional.ofNullable(model.get(EquipmentGroups.FEET))),
                                    ENTRY_CODEC.optionalFieldOf("feet").forGetter(model -> Optional.ofNullable(model.get(EquipmentGroups.FEET))),
                                    ENTRY_CODEC.optionalFieldOf("legs").forGetter(model -> Optional.ofNullable(model.get(EquipmentGroups.LEGS))),
                                    ENTRY_CODEC.optionalFieldOf("chest").forGetter(model -> Optional.ofNullable(model.get(EquipmentGroups.CHEST))),
                                    ENTRY_CODEC.optionalFieldOf("head").forGetter(model -> Optional.ofNullable(model.get(EquipmentGroups.HEAD))),
                                    ENTRY_CODEC.optionalFieldOf("armor").forGetter(model -> Optional.ofNullable(model.get(EquipmentGroups.ARMOR))),
                                    ENTRY_CODEC.optionalFieldOf("body").forGetter(model -> Optional.ofNullable(model.get(EquipmentGroups.BODY))),
                                    ENTRY_CODEC.optionalFieldOf("torso").forGetter(model -> Optional.ofNullable(model.get(EquipmentGroups.TORSO))),
                                    ENTRY_CODEC.optionalFieldOf("offhand").forGetter(model -> Optional.ofNullable(model.get(EquipmentGroups.OFFHAND))),
                                    ENTRY_CODEC.optionalFieldOf("at_ready").forGetter(model -> Optional.ofNullable(model.get(EquipmentGroups.AT_READY)))
                        ).apply(in, (any, f, l, c, h, a, b, t, o, r) -> {
                              Model model = new Model();
                              any.ifPresent(model::defaultReturnValue);
                              putIn(model, EquipmentGroups.FEET, f);
                              putIn(model, EquipmentGroups.LEGS, l);
                              putIn(model, EquipmentGroups.CHEST, c);
                              putIn(model, EquipmentGroups.HEAD, h);
                              putIn(model, EquipmentGroups.ARMOR, a);
                              putIn(model, EquipmentGroups.BODY, b);
                              putIn(model, EquipmentGroups.TORSO, t);
                              putIn(model, EquipmentGroups.OFFHAND, o);
                              putIn(model, EquipmentGroups.AT_READY, r);
                              return model;
                        })
            );
      }
      
      public static final Codec<EquipmentModel> CODEC = Codec.withAlternative(Model.CODEC.xmap(model -> new EquipmentModel(model, false), EquipmentModel::model), 
                  Codec.BOOL.xmap(
                              bool -> new EquipmentModel(new Model(), true), 
                              equipmentModel -> equipmentModel.useBuiltInLeatherModel
                  )
      );
      
      public static final StreamCodec<RegistryFriendlyByteBuf, EquipmentModel> STREAM_CODEC = new StreamCodec<>() {

            @Override
            public void encode(RegistryFriendlyByteBuf buf, EquipmentModel equipmentModel) {
                  buf.writeBoolean(equipmentModel.useBuiltInLeatherModel);
                  if (equipmentModel.useBuiltInLeatherModel)
                        return;

                  Model model = equipmentModel.model;
                  int size = model.size();
                  buf.writeInt(size);
                  model.forEach((groups, map) -> {
                        buf.writeEnum(groups);
                        buf.writeMap(map, FriendlyByteBuf::writeEnum, FriendlyByteBuf::writeResourceLocation);
                  });
            }

            @Override
            public EquipmentModel decode(RegistryFriendlyByteBuf buf) {
                  Model model = new Model();
                  if (buf.readBoolean())
                        return new EquipmentModel(model, true);

                  int size = buf.readInt();
                  for (int i = 0; i < size; i++) {
                        EquipmentGroups equipmentGroups = buf.readEnum(EquipmentGroups.class);
                        Map<Attachment, ResourceLocation> attachmentResourceLocationMap = buf.readMap(buf1 -> buf1.readEnum(Attachment.class), FriendlyByteBuf::readResourceLocation);
                        HashMap<Attachment, ResourceLocation> map = new HashMap<>(attachmentResourceLocationMap);
                        model.put(equipmentGroups, map);
                  }

                  return new EquipmentModel(model, false);
            }
      };

      public enum Attachment {
            HEAD(),
            BODY(),
            R_ARM(),
            L_ARM(),
            R_LEG(),
            L_LEG(),
      }
}
