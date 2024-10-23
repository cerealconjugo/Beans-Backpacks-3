package com.beansgalaxy.backpacks.util;

import com.beansgalaxy.backpacks.Constants;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface IntRepresentable {

      int getOpcode();



      static <E extends Enum<E> & IntRepresentable> Codec<E> fromEnum(Supplier<E[]> pElementsSupplier) {
            return PrimitiveCodec.INT.flatXmap(integer -> {
                              E e = testMatch(integer, pElementsSupplier);
                              if (e != null)
                                    return DataResult.success(e);
                              return DataResult.error(() -> "Int [" + integer + "] is not within bounds");
            }, pEnum -> DataResult.success(pEnum.getOpcode()));
      }

      static <E extends Enum<E> & IntRepresentable> Codec<E> fromEnum(Supplier<E[]> pElementsSupplier, int min, int max) {
            return PrimitiveCodec.INT.flatXmap(integer -> {
                              if (integer >= min & integer <= max) {
                                    E e = testMatch(integer, pElementsSupplier);
                                    if (e != null)
                                          return DataResult.success(e);
                              }
                              return DataResult.error(() -> "Int [" + integer + "] is not within bounds; " + min + '-' + max);
            }, pEnum -> DataResult.success(pEnum.getOpcode()));
      }

      static <E extends Enum<E> & IntRepresentable> E testMatch(Predicate<E> predicate, Supplier<E[]> pElementsSupplier) {
            return IntRepresentable.testMatch(predicate, pElementsSupplier, null);
      }

      static <E extends Enum<E> & IntRepresentable> E testMatch(Predicate<E> predicate, Supplier<E[]> pElementsSupplier, E defau) {
            Iterator<E> iterator = Arrays.stream(pElementsSupplier.get()).iterator();
            while (iterator.hasNext()) {
                  E next = iterator.next();
                  if (predicate.test(next)) {
                        return next;
                  }
            }
            return defau;
      }

      static <E extends Enum<E> & IntRepresentable> E testMatch(Integer integer, Supplier<E[]> pElementsSupplier) {
            return IntRepresentable.testMatch(e -> e.getOpcode() == integer, pElementsSupplier);
      }


      static <E extends Enum<E> & IntRepresentable> StreamCodec<RegistryFriendlyByteBuf, E> fromEnumStream(Supplier<E[]> pElementsSupplier, E defau, String name) {
            return new StreamCodec<>() {
                  @Override @NotNull
                  public E decode(@NotNull RegistryFriendlyByteBuf buf) {
                        int i = buf.readInt();
                        E e = testMatch(i, pElementsSupplier);
                        if (e == null) {
                              Constants.LOG.error("Error decoding {}[{}] as IntRepresentable; This shouldn't happen!!", name, i);
                              return defau;
                        }
                        return e;
                  }

                  @Override
                  public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull E representable) {
                        buf.writeInt(representable.getOpcode());
                  }
            };
      }
}
