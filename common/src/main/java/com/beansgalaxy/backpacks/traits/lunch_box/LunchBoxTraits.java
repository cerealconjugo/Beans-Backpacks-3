package com.beansgalaxy.backpacks.traits.lunch_box;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.SlotSelection;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.function.Consumer;

public class LunchBoxTraits extends BundleLikeTraits {
      public static final String NAME = "lunch";
      private final List<ItemStack> nonEdibles;
      private final LunchBoxFields fields;
      private List<ItemStack> stacks;

      public LunchBoxTraits(LunchBoxFields fields, List<ItemStack> stacks, List<ItemStack> nonEdibles) {
            super(Traits.getWeight(stacks, fields.size()));
            this.fields = fields;
            this.nonEdibles = nonEdibles;
            this.stacks = stacks;
      }

      public LunchBoxTraits(LunchBoxTraits traits, List<ItemStack> stacks, List<ItemStack> nonEdibles) {
            this(traits.fields, traits.slotSelection, stacks, nonEdibles);
      }

      public LunchBoxTraits(LunchBoxFields fields, SlotSelection selection, List<ItemStack> stacks, List<ItemStack> nonEdibles) {
            super(Traits.getWeight(stacks, fields.size()), selection);
            this.fields = fields;
            this.nonEdibles = nonEdibles;
            this.stacks = stacks;
      }

      @Override
      public LunchBoxFields fields() {
            return fields;
      }

      @Override
      public int size() {
            return fields.size();
      }

      @Override
      public List<ItemStack> stacks() {
            return this.stacks;
      }

      protected List<ItemStack> nonEdibles() {
            return nonEdibles;
      }

      @Override
      public IClientTraits client() {
            return LunchBoxClient.INSTANCE;
      }

      @Override
      public LunchBoxTraits toReference(ResourceLocation location) {
            return new LunchBoxTraits(fields.toReference(location), slotSelection, stacks, nonEdibles);
      }

      @Override
      public String name() {
            return NAME;
      }

      public static void ifPresent(ItemStack lunchBox, Consumer<LunchBoxTraits> ifPresent) {
            LunchBoxTraits boxTraits = lunchBox.get(Traits.LUNCH_BOX);
            if (boxTraits != null) {
                  ifPresent.accept(boxTraits);
                  return;
            }

            ReferenceTrait referenceTrait = lunchBox.get(Traits.REFERENCE);
            if (referenceTrait == null || referenceTrait.isEmpty())
                  return;

            referenceTrait.getTrait().ifPresent(traits -> {
                  if (traits instanceof LunchBoxTraits lunchBoxTraits)
                        ifPresent.accept(lunchBoxTraits);
            });
      }

      public static void firstIsPresent(ItemStack lunchBox, LivingEntity entity, Consumer<ItemStack> ifPresent) {
            ifPresent(lunchBox, traits -> {
                  List<ItemStack> stacks = traits.stacks();
                  if (!stacks.isEmpty()) {
                        int selectedSlotSafe = entity instanceof Player player
                                    ? traits.getSelectedSlotSafe(player)
                                    : 0;

                        ifPresent.accept(stacks.get(selectedSlotSafe));
                  }
            });
      }

      public static void firstIsPresent(ItemStack lunchBox, Consumer<ItemStack> ifPresent) {
            ifPresent(lunchBox, traits -> {
                  List<ItemStack> stacks = traits.stacks();
                  if (!stacks.isEmpty()) {
                        int selectedSlotSafe = 0; //TODO FIND A NEW WAY TO COLLECT THE SELECTED SLOT
                        ifPresent.accept(stacks.get(selectedSlotSafe));
                  }
            });
      }

      public void finishUsingItem(ItemStack backpack, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
            Mutable mutable = mutable();
            int selectedSlot = entity instanceof Player player ? getSelectedSlotSafe(player) : 0;
            ItemStack stack = mutable.getItemStacks().get(selectedSlot).split(1);
            ItemStack consumedStack = stack.finishUsingItem(level, entity);
            ItemStack itemStack = mutable.addItem(consumedStack, null);
            if (itemStack == null)
                  mutable.addNonEdible(consumedStack);

            freezeAndCancel(PatchedComponentHolder.of(backpack), mutable);
            cir.setReturnValue(backpack);
      }

      @Override
      public void use(Level level, Player player, InteractionHand hand, ItemStack backpack, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
            if (stacks().isEmpty())
                  return;

            int selectedSlotSafe = getSelectedSlotSafe(player);
            ItemStack first = stacks().get(selectedSlotSafe);
            FoodProperties $$4 = first.get(DataComponents.FOOD);
            if ($$4 != null) {
                  if (player.canEat($$4.canAlwaysEat())) {
                        player.startUsingItem(hand);
                        cir.setReturnValue(InteractionResultHolder.consume(backpack));
                  }
            }
      }

      @Override
      public Mutable mutable() {
            return new Mutable();
      }

      private record LunchBoxData(List<ItemStack> stacks, List<ItemStack> nonEdibles) {
            public static final Codec<LunchBoxData> CODEC = RecordCodecBuilder.create(in ->
                        in.group(
                                    ItemStack.CODEC.listOf().fieldOf("items").forGetter(LunchBoxData::stacks),
                                    ItemStack.CODEC.listOf().optionalFieldOf("nonEdibles", List.of()).forGetter(LunchBoxData::nonEdibles)
                        ).apply(in, LunchBoxData::new)
            );
      }

      public static final Codec<LunchBoxTraits> CODEC = RecordCodecBuilder.create(in ->
                  in.group(
                              PrimitiveCodec.INT.fieldOf("size").forGetter(LunchBoxTraits::size),
                              ModSound.MAP_CODEC.forGetter(LunchBoxTraits::sound),
                              LunchBoxData.CODEC.optionalFieldOf("data", new LunchBoxData(List.of(), List.of())).forGetter(traits -> new LunchBoxData(traits.stacks(), traits.nonEdibles))
                  ).apply(in, (size, sound, data) -> new LunchBoxTraits(new LunchBoxFields(size, sound), data.stacks, data.nonEdibles))
      );

      public static final StreamCodec<RegistryFriendlyByteBuf, LunchBoxTraits> STREAM_CODEC = StreamCodec.of((buf, traits) -> {
            LunchBoxFields.STREAM_CODEC.encode(buf, traits.fields);
            ItemStack.LIST_STREAM_CODEC.encode(buf, traits.stacks());
            ItemStack.LIST_STREAM_CODEC.encode(buf, traits.nonEdibles);
      }, buf -> {
            LunchBoxFields declared = LunchBoxFields.STREAM_CODEC.decode(buf);
            List<ItemStack> stacks = ItemStack.LIST_STREAM_CODEC.decode(buf);
            List<ItemStack> nonEdibles = ItemStack.LIST_STREAM_CODEC.decode(buf);
            return new LunchBoxTraits(declared, stacks, nonEdibles);
      });

      @Override
      public boolean canItemFit(ItemStack inserted) {
            return inserted.has(DataComponents.FOOD) && super.canItemFit(inserted);
      }

      public class Mutable extends MutableBundleLike {
            private final ArrayList<ItemStack> nonEdibles;

            public Mutable() {
                  super(LunchBoxTraits.this);
                  nonEdibles = new ArrayList<>(LunchBoxTraits.this.nonEdibles);
            }

            @Override
            public LunchBoxTraits freeze() {
                  List<ItemStack> stacks = getItemStacks();
                  stacks.removeIf(ItemStack::isEmpty);
                  return new LunchBoxTraits(LunchBoxTraits.this, stacks, nonEdibles.stream().toList());
            }

            @Override
            public void dropItems(Entity backpackEntity) {
                  while (!isEmpty()) {
                        ItemStack stack = removeItemNoUpdate(0);
                        backpackEntity.spawnAtLocation(stack);
                  }
            }

            HashMap<ServerPlayer, Integer> playersEating = new HashMap<>();

            @Override
            public void entityTick(BackpackEntity backpackEntity) {
                  for (Map.Entry<ServerPlayer, Integer> entry : playersEating.entrySet()) {
                        ServerPlayer key = entry.getKey();
                        Integer value = entry.getValue();

                        if (value < 0)
                              playersEating.remove(key);
                        else
                              playersEating.put(key, value - 1);
                  }
            }

            @Override
            public InteractionResult interact(BackpackEntity backpackEntity, Player player, InteractionHand hand) {
                  List<ItemStack> stacks = getItemStacks();
                  if (stacks.isEmpty())
                        return InteractionResult.PASS;

                  if (!player.canEat(false)) {
                        return InteractionResult.PASS;
                  }

                  Level level = player.level();
                  ItemStack first = stacks.getFirst();

                  if (!first.isEmpty()) {
                        if (first.getUseAnimation() == UseAnim.DRINK) {
                              player.playSound(first.getDrinkingSound(), 0.5F, player.getRandom().nextFloat() * 0.1F + 0.9F);
                        }

                        if (first.getUseAnimation() == UseAnim.EAT) {
                              for (int i = 0; i < 5; i++) {
                                    Vec3 vec3 = new Vec3((player.getRandom().nextDouble() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0);
                                    vec3 = vec3.xRot(-player.getXRot() * (float) (Math.PI / 180.0));
                                    vec3 = vec3.yRot(-player.getYRot() * (float) (Math.PI / 180.0));
                                    double d0 = -player.getRandom().nextDouble() * 0.6 - 0.3;
                                    Vec3 vec31 = new Vec3((player.getRandom().nextDouble() - 0.5) * 0.3, d0, 0.6);
                                    vec31 = vec31.xRot(-player.getXRot() * (float) (Math.PI / 180.0));
                                    vec31 = vec31.yRot(-player.getYRot() * (float) (Math.PI / 180.0));
                                    vec31 = vec31.add(player.getX(), player.getEyeY(), player.getZ());
                                    level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, first), vec31.x, vec31.y, vec31.z, vec3.x, vec3.y + 0.05, vec3.z);
                              }

                              player.playSound(first.getEatingSound(), 0.5F + 0.5F * (float)player.getRandom().nextInt(2), (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F + 1.0F);
                        }
                  }

                  if (player instanceof ServerPlayer serverPlayer) {
                        if (!playersEating.containsKey(serverPlayer)) {
                              playersEating.put(serverPlayer, 3);
                        } else {
                              int i = playersEating.get(serverPlayer);
                              if (i < first.getUseDuration(player))
                                    playersEating.put(serverPlayer, i + 12);
                              else {
                                    ItemStack stack = first.split(1);
                                    ItemStack consumedStack = stack.finishUsingItem(level, player);
                                    ItemStack itemStack = addItem(consumedStack, null);
                                    if (itemStack == null)
                                          addNonEdible(consumedStack);

                                    if (first.isEmpty())
                                          stacks.removeFirst();

                                    playersEating.remove(serverPlayer);
                              }
                        }
                  }
                  
                  return InteractionResult.SUCCESS;
            }

            private void addNonEdible(ItemStack consumedStack) {
                  if (!consumedStack.isEmpty()) {
                        for (ItemStack nonEdible : nonEdibles) {
                              if (ItemStack.isSameItemSameComponents(nonEdible, consumedStack)) {
                                    int insert = Math.min(nonEdible.getMaxStackSize() - nonEdible.getCount(), consumedStack.getCount());
                                    nonEdible.grow(insert);
                                    consumedStack.shrink(insert);
                              }
                        }

                        if (!consumedStack.isEmpty()) {
                              nonEdibles.add(consumedStack);
                        }
                  }
            }

            @Override
            public @NotNull ItemStack removeItemNoUpdate(int slot) {
                  if (!nonEdibles.isEmpty())
                        return nonEdibles.removeFirst();

                  return super.removeItemNoUpdate(slot);
            }

            @Override
            public LunchBoxTraits trait() {
                  return LunchBoxTraits.this;
            }
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LunchBoxTraits traits)) return false;
            return Objects.equals(nonEdibles, traits.nonEdibles) && Objects.equals(fields, traits.fields) && Objects.equals(stacks, traits.stacks);
      }

      @Override
      public int hashCode() {
            return Objects.hash(nonEdibles, fields, stacks);
      }

      @Override
      public String toString() {
            return "LunchBoxTraits{" +
                        "nonEdibles=" + nonEdibles +
                        ", fields=" + fields +
                        ", stacks=" + stacks +
                        '}';
      }
}
