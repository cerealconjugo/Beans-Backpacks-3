package com.beansgalaxy.backpacks.components.reference;

import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.IDeclaredFields;
import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.generic.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class NonTrait extends IDeclaredFields implements GenericTraits, GenericTraits.MutableTraits, ITraitCodec<NonTrait, NonTrait> {
      public static final String NAME = "non";
      public static final NonTrait INSTANCE = new NonTrait();
      public static final TraitComponentKind<NonTrait, ? extends IDeclaredFields> KIND = new TraitComponentKind<>(-1, NAME, INSTANCE);

      private NonTrait() {
            super(null, null);
      }

      public static <T> boolean is(T trait) {
            return INSTANCE.equals(trait);
      }

      @Override @NotNull
      public NonTrait asBlankTrait() {
            return this;
      }

      @Override
      public TraitComponentKind<NonTrait, ? extends IDeclaredFields> kind() {
            return KIND;
      }

      @Override
      public NonTrait toReference(ResourceLocation location) {
            return this;
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public IDeclaredFields fields() {
            return this;
      }

      @Override
      public IClientTraits client() {
            return NonTraitClient.INSTANCE;
      }

      @Override
      public int size() {
            return 0;
      }

      @Override
      public Fraction fullness() {
            return Fraction.ONE;
      }

      @Override
      public void stackedOnMe(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {

      }

      @Override
      public void stackedOnOther(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir) {

      }

      @Override
      public boolean isEmpty() {
            return true;
      }

      @Override
      public MutableTraits mutable() {
            return this;
      }

      public static final Codec<NonTrait> CODEC = new Codec<>() {

            @Override
            public <T> DataResult<Pair<NonTrait, T>> decode(DynamicOps<T> ops, T input) {
                  return DataResult.success(Pair.of(INSTANCE, input));
            }

            @Override
            public <T> DataResult<T> encode(NonTrait input, DynamicOps<T> ops, T prefix) {
                  return DataResult.success(prefix);
            }
      };

      @Override
      public Codec<NonTrait> codec() {
            return CODEC;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, NonTrait> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public NonTrait decode(RegistryFriendlyByteBuf buf) {
                  return INSTANCE;
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, NonTrait trait) {

            }
      };

      @Override
      public StreamCodec<RegistryFriendlyByteBuf, NonTrait> streamCodec() {
            return STREAM_CODEC;
      }

      @Override
      public Codec<NonTrait> fieldCodec() {
            return codec();
      }

      @Override
      public GenericTraits freeze() {
            return this;
      }

      @Override @Nullable
      public ItemStack addItem(ItemStack stack, Player player) {
            return null;
      }

      @Override
      public ItemStack removeItemNoUpdate(ItemStack carried, Player player) {
            return carried;
      }

      @Override
      public void dropItems(Entity backpackEntity) {

      }

      @Override
      public InteractionResult interact(BackpackEntity backpackEntity, Player player, InteractionHand hand) {
            return InteractionResult.PASS;
      }

      @Override
      public GenericTraits trait() {
            return this;
      }

      @Override
      public String toString() {
            return "NonTrait{}";
      }

      @Override
      public boolean equals(Object o) {
            return o == this;
      }
}
