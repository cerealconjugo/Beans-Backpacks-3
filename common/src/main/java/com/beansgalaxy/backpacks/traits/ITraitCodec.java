package com.beansgalaxy.backpacks.traits;

import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface ITraitCodec<A extends GenericTraits> {
      Codec<A> codec();

      StreamCodec<RegistryFriendlyByteBuf, A> streamCodec();

      default <T> DataResult<T> encode(final A input, final DynamicOps<T> ops, final T prefix) {
            return codec().encode(input, ops, prefix);
      }

      default <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
            return codec().decode(ops, input);
      }

      default void encode(RegistryFriendlyByteBuf buf, A trait) {
            streamCodec().encode(buf, trait);
      }

      default A decode(RegistryFriendlyByteBuf buf) {
            return streamCodec().decode(buf);
      }
}