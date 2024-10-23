package com.beansgalaxy.backpacks.traits;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.components.reference.NonTrait;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;

public class TraitComponentKind<T extends GenericTraits, F extends IDeclaredFields> implements DataComponentType<T> {
      private static final HashSet<TraitComponentKind<? extends GenericTraits, ? extends IDeclaredFields>> TRAITS = new HashSet<>();
      public static final StreamCodec<RegistryFriendlyByteBuf, TraitComponentKind<? extends GenericTraits, ? extends IDeclaredFields>> STREAM_CODEC =
                  StreamCodec.of((buf, type) -> buf.writeInt(type.i), buf -> {
                        int integer = buf.readInt();
                        return get(integer);
                  });

      public static final Codec<TraitComponentKind<? extends GenericTraits, ? extends IDeclaredFields>> CODEC = PrimitiveCodec.INT.comapFlatMap(
                              integer -> {
                                    TraitComponentKind<? extends GenericTraits, ? extends IDeclaredFields> type = get(integer);
                                    return type == null ? DataResult.error(() -> "failure to parse TraitComponentType")
                                                : DataResult.success(type);
                              }, type -> type.i
      );

      public static TraitComponentKind<? extends GenericTraits, ? extends IDeclaredFields> get(int integer) {
            for (TraitComponentKind<? extends GenericTraits, ? extends IDeclaredFields> trait : TRAITS)
                  if (trait.i == integer)
                        return trait;
            return NonTrait.KIND;
      }

      public static TraitComponentKind<? extends GenericTraits, ? extends IDeclaredFields>  get(String kind) {
            for (TraitComponentKind<? extends GenericTraits, ? extends IDeclaredFields> trait : TRAITS)
                  if (Objects.equals(trait.name, kind))
                        return trait;
            return NonTrait.KIND;
      }

      public final int i;
      private final String name;
      private final ITraitCodec<T, F> codecs;

      public TraitComponentKind(int i, String name, ITraitCodec<T, F> codecs)
      {
            this.i = i;
            this.name = name;
            this.codecs = codecs;
      }

      public static <T extends GenericTraits, F extends IDeclaredFields> TraitComponentKind<T, ? extends IDeclaredFields> register(String name, ITraitCodec<T, F> codecs) {
            int i = TraitComponentKind.TRAITS.size();
            TraitComponentKind<T, F> componentType = new TraitComponentKind<>(i, name, codecs);
            TRAITS.add(componentType);
            return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                        ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name),
                        componentType);
      }

      @Override @NotNull
      public Codec<T> codec() {
            return codecs.codec();
      }

      @Override
      public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
            return codecs.streamCodec();
      }

      @NotNull
      public Codec<F> declaredCodec() {
            return codecs.fieldCodec();
      }

      public String toString() {
            return "TraitComponentKind[" + name + ']';
      }

      public <M extends GenericTraits.MutableTraits> void freezeAndCancel(PatchedComponentHolder holder, M mutable) {
            T freeze = (T) mutable.freeze();
            save(holder, freeze);
      }

      public void save(PatchedComponentHolder holder, T trait) {
            holder.set(this, trait);
      }

      public void encode(RegistryFriendlyByteBuf buf, T traits) {
            codecs.streamCodec().encode(buf, traits);
      }

      public <A> RecordBuilder<A> encode(T traits, DynamicOps<A> ops, RecordBuilder<A> prefix) {
            return codec().fieldOf("trait").encode(traits, ops, prefix);
      }

}
