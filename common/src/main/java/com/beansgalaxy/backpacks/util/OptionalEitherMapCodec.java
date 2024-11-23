package com.beansgalaxy.backpacks.util;

import com.beansgalaxy.backpacks.components.equipable.EquipmentModel;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.*;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.stream.Stream;

public class OptionalEitherMapCodec<L, R> extends MapCodec<Optional<Either<L, R>>> {
      private static final String MODEL = "equipment_model";
      private static final String TEXTURE = "backpack_texture";
      private static final MapCodec<Either<EquipmentModel, ResourceLocation>> EITHER = Codec.mapEither(
                  EquipmentModel.CODEC.fieldOf(MODEL),
                  ResourceLocation.CODEC.fieldOf(TEXTURE)
      );
      private final MapCodec<Either<L, R>> either;
      private final String leftKey;
      private final String rightKey;

      public OptionalEitherMapCodec(String leftKey, Codec<L> left, String rightKey, Codec<R> right) {
            this.leftKey = leftKey;
            this.rightKey = rightKey;
            either = Codec.mapEither(left.fieldOf(leftKey), right.fieldOf(rightKey));
      }

      @Override
      public <T> Stream<T> keys(DynamicOps<T> ops) {
            return either.keys(ops);
      }

      @Override
      public <T> DataResult<Optional<Either<L, R>>> decode(DynamicOps<T> ops, MapLike<T> input) {
            T model = input.get(leftKey);
            T texture = input.get(rightKey);

            if (model == null && texture == null)
                  return DataResult.success(Optional.empty());

            DataResult<Either<L, R>> decode = either.decode(ops, input);
            return decode.map(Optional::of).setPartial(Optional::empty);
      }

      @Override
      public <T> RecordBuilder<T> encode(Optional<Either<L, R>> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            if (input.isPresent())
                  return either.encode(input.get(), ops, prefix);
            return prefix;
      }
}
