package com.beansgalaxy.backpacks.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

public final class EmptyStack {
      private final DataComponentPatch data;
      public int amount;

      public EmptyStack(int amount, DataComponentPatch data) {
            this.amount = amount;
            this.data = data;
      }

      public int amount() {
            return amount;
      }

      public DataComponentPatch data() {
            return data;
      }

      public static int count(List<EmptyStack> stacks) {
            return stacks.stream().mapToInt(EmptyStack::amount).sum();
      }

      public static Fraction weight(List<EmptyStack> stacks, Holder<Item> item) {
            int count = 0;
            int maxStackSize = 0;
            for (EmptyStack stack : stacks) {
                  count += stack.getCount();
                  maxStackSize += stack.getMaxStackSize(item);
            }

            return Fraction.getFraction(count, maxStackSize);
      }

      public static Fraction maxSize(List<EmptyStack> stacks, Holder<Item> item) {
            int maxStackSize = stacks.stream().mapToInt(stack -> stack.getMaxStackSize(item)).sum();
            return Fraction.getFraction(maxStackSize, stacks.size());
      }

      @Override
      public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (EmptyStack) obj;
            return this.amount == that.amount &&
                        Objects.equals(this.data, that.data);
      }

      @Override
      public int hashCode() {
            return Objects.hash(amount, data);
      }

      @Override
      public String toString() {
            return "EmptyStack[" +
                        "amount=" + amount + ", " +
                        "data=" + data + ']';
      }

      public int getMaxStackSize(Holder<Item> itemHolder) {
            Optional<? extends Integer> maxStackSize = data.get(DataComponents.MAX_STACK_SIZE);
            if (maxStackSize != null && maxStackSize.isPresent())
                  return maxStackSize.get();

            return itemHolder.value().getDefaultMaxStackSize();
      }

      public ItemStack withCappedStackSize(Holder<Item> itemHolder) {
            int maxStackSize = getMaxStackSize(itemHolder);
            int count = Math.min(maxStackSize, amount);
            return new ItemStack(itemHolder, count, data);
      }

      public int getCount() {
            return amount;
      }

      public ItemStack splitItem(Holder<Item> itemHolder, int amount) {
            int count = Math.min(amount, this.amount);
            this.amount -= count;
            return withItem(itemHolder, amount);
      }

      public ItemStack withItem(Holder<Item> itemHolder, int amount) {
            return new ItemStack(itemHolder, amount, data);
      }

      public ItemStack withItem(Holder<Item> itemHolder) {
            return withItem(itemHolder, amount);
      }

      public boolean isEmpty() {
            return amount == 0;
      }

      public boolean is(ItemStack inserted) {
            return Objects.equals(inserted.getComponentsPatch(), data);
      }

      public static final Codec<EmptyStack> EMPTY_STACK_CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(
                  $$0 -> $$0.group(
                              ExtraCodecs.POSITIVE_INT.fieldOf("count").orElse(1).forGetter(EmptyStack::amount),
                              DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(EmptyStack::data)
                  ).apply($$0, EmptyStack::new)
      ));

      public static final StreamCodec<RegistryFriendlyByteBuf, EmptyStack> EMPTY_STACK_STREAM_CODEC = new StreamCodec<>() {
            @Override
            @NotNull
            public EmptyStack decode(RegistryFriendlyByteBuf buf) {
                  int amount = buf.readInt();
                  DataComponentPatch data = DataComponentPatch.STREAM_CODEC.decode(buf);
                  return new EmptyStack(amount, data);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, EmptyStack ctx) {
                  buf.writeInt(ctx.amount);
                  DataComponentPatch.STREAM_CODEC.encode(buf, ctx.data);
            }
      };

      public static final StreamCodec<RegistryFriendlyByteBuf, List<EmptyStack>> LIST_EMPTY_STACK_STREAM_CODEC = EMPTY_STACK_STREAM_CODEC.apply(
                  ByteBufCodecs.collection(NonNullList::createWithCapacity)
      );

      public EmptyStack copyWithCount(int remainder) {
            return new EmptyStack(remainder, data);
      }
}
