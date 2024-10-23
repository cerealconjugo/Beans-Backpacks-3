package com.beansgalaxy.backpacks.traits.bulk;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class BulkTraits extends BundleLikeTraits {
      public static final String NAME = "bulk";
      private final BulkFields fields;
      protected final Holder<Item> item;
      protected final List<EmptyStack> emptyStacks;

      public BulkTraits(BulkFields fields, Holder<Item> item, List<EmptyStack> emptyStacks) {
            super(Traits.getWeight(stacks(item, emptyStacks), fields.size()));
            this.fields = fields;
            this.item = item;
            this.emptyStacks = emptyStacks;
      }

      public BulkTraits(BulkTraits traits, Holder<Item> item, List<EmptyStack> emptyStacks) {
            super(Traits.getWeight(stacks(item, emptyStacks), traits.fields.size()), traits.slotSelection);
            this.fields = traits.fields;
            this.item = item;
            this.emptyStacks = emptyStacks;
      }

      @Override
      public IClientTraits client() {
            return BulkClient.INSTANCE;
      }

      @Override
      public BulkTraits toReference(ResourceLocation location) {
            return new BulkTraits(fields.toReference(location), item, emptyStacks);
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public BulkFields fields() {
            return fields;
      }

      @Override
      public int size() {
            return fields.size();
      }

      @Override
      public List<ItemStack> stacks() {
            return stacks(item, emptyStacks);
      }

      public int amount() {
            return emptyStacks.stream().mapToInt(EmptyStack::amount).sum();
      }

      private static List<ItemStack> stacks(Holder<Item> item, List<EmptyStack> emptyStacks) {
            return emptyStacks.stream().map(
                        empty -> new ItemStack(item, empty.amount, empty.data)
            ).toList();
      }

      public int getSelectedSlot(Player player) {
            return 0;
      }

      public int getSelectedSlotSafe(Player player) {
            return 0;
      }

      public void setSelectedSlot(Player sender, int selectedSlot) {

      }

      public void limitSelectedSlot(int slot, int size) {

      }

      public record EmptyStack(int amount, DataComponentPatch data) {
            public static final Codec<EmptyStack> EMPTY_STACK_CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(
                        $$0 -> $$0.group(
                                    ExtraCodecs.POSITIVE_INT.fieldOf("count").orElse(1).forGetter(EmptyStack::amount),
                                    DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(EmptyStack::data)
                        ).apply($$0, EmptyStack::new)
            ));

            public static final StreamCodec<RegistryFriendlyByteBuf, EmptyStack> EMPTY_STACK_STREAM_CODEC = new StreamCodec<>() {
                  @Override @NotNull
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
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, BulkTraits> STREAM_CODEC = StreamCodec.of((buf, traits) -> {
            BulkFields.STREAM_CODEC.encode(buf, traits.fields);
            BulkData.STREAM_CODEC.encode(buf, new BulkData(traits.item, traits.emptyStacks));
      }, buf -> {
            BulkFields fields = BulkFields.STREAM_CODEC.decode(buf);
            BulkData data = BulkData.STREAM_CODEC.decode(buf);
            return new BulkTraits(fields, data.item, data.stacks);
      });

      protected record BulkData(Holder<Item> item, List<EmptyStack> stacks) {
            public static final Codec<BulkData> CODEC = RecordCodecBuilder.create(in ->
                        in.group(
                                    BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("item").forGetter(BulkData::item),
                                    EmptyStack.EMPTY_STACK_CODEC.listOf().fieldOf("stacks").forGetter(BulkData::stacks)
                        ).apply(in, BulkData::new)
            );

            public static final StreamCodec<RegistryFriendlyByteBuf, BulkData> STREAM_CODEC = new StreamCodec<>() {
                  @Override
                  public void encode(RegistryFriendlyByteBuf buf, BulkData bulkData) {
                        ByteBufCodecs.holderRegistry(Registries.ITEM).encode(buf, bulkData.item);
                        EmptyStack.LIST_EMPTY_STACK_STREAM_CODEC.encode(buf, bulkData.stacks);
                  }

                  @Override @NotNull
                  public BulkData decode(RegistryFriendlyByteBuf buf) {
                        Holder<Item> item = ByteBufCodecs.holderRegistry(Registries.ITEM).decode(buf);
                        List<EmptyStack> stacks = EmptyStack.LIST_EMPTY_STACK_STREAM_CODEC.decode(buf);
                        return new BulkData(item, stacks);
                  }
            };
      }

      public static final Codec<BulkTraits> CODEC = RecordCodecBuilder.create(in ->
            in.group(
                        PrimitiveCodec.INT.fieldOf("size").forGetter(BulkTraits::size),
                        ModSound.MAP_CODEC.forGetter(BulkTraits::sound),
                        BulkData.CODEC.optionalFieldOf("data", new BulkData(BuiltInRegistries.ITEM.wrapAsHolder(Items.AIR), List.of())).forGetter(traits -> new BulkData(traits.item, traits.emptyStacks))
            ).apply(in, (size, sound, data) -> new BulkTraits(new BulkFields(size, sound), data.item, data.stacks))
      );

      @Override
      public Mutable mutable() {
            return new Mutable();
      }

      @Override
      public boolean isEmpty() {
            return BuiltInRegistries.ITEM.wrapAsHolder(Items.AIR).equals(item) || emptyStacks.isEmpty();
      }

      @Override
      public boolean canItemFit(ItemStack inserted) {
            return super.canItemFit(inserted) && (isEmpty() || inserted.is(item));
      }

      public class Mutable extends MutableBundleLike {
            public Mutable() {
                  super(BulkTraits.this);
            }

            @Override
            public BulkTraits freeze() {
                  List<ItemStack> stacks = getItemStacks();
                  stacks.removeIf(ItemStack::isEmpty);
                  if (stacks.isEmpty())
                        return new BulkTraits(BulkTraits.this, BuiltInRegistries.ITEM.wrapAsHolder(Items.AIR), List.of());

                  Holder<Item> item = BuiltInRegistries.ITEM.wrapAsHolder(stacks.getFirst().getItem());
                  List<EmptyStack> emptyStacks = stacks.stream().map(stack -> new EmptyStack(stack.getCount(), stack.getComponentsPatch())).toList();
                  return new BulkTraits(BulkTraits.this, item, emptyStacks);
            }

            @Override
            public void dropItems(Entity backpackEntity) {
                  while (!isEmpty()) {
                        ItemStack stack = removeItemNoUpdate(0);
                        backpackEntity.spawnAtLocation(stack);
                  }
            }

            @Override
            public InteractionResult interact(BackpackEntity backpackEntity, Player player, InteractionHand hand) {
                  ItemStack other = player.getItemInHand(hand);

                  if (other.isEmpty()) {
                        if (isEmpty() || InteractionHand.OFF_HAND.equals(hand))
                              return InteractionResult.PASS;

                        ItemStack stack = removeItemNoUpdate(other, player);
                        if (stack != null) {
                              player.setItemInHand(hand, stack);
                              sound().at(player, ModSound.Type.REMOVE);
                              backpackEntity.wobble = 8;
                              return InteractionResult.SUCCESS;
                        }
                  }
                  else if (addItem(other, player) == null)
                        return InteractionResult.PASS;

                  backpackEntity.wobble = 8;
                  sound().at(player, ModSound.Type.INSERT);
                  return InteractionResult.SUCCESS;
            }

            @Override
            public BulkTraits trait() {
                  return BulkTraits.this;
            }
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BulkTraits that)) return false;
            return Objects.equals(fields, that.fields) && Objects.equals(item, that.item) && Objects.equals(emptyStacks, that.emptyStacks);
      }

      @Override
      public int hashCode() {
            return Objects.hash(fields, item, emptyStacks);
      }

      @Override
      public String toString() {
            return "BulkTraits{" +
                        "fields=" + fields +
                        ", item=" + item +
                        ", emptyStacks=" + emptyStacks +
                        '}';
      }
}
