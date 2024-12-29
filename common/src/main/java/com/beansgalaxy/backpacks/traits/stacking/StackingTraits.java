package com.beansgalaxy.backpacks.traits.stacking;

import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.traits.IEntityTraits;
import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.MutableTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

public class StackingTraits extends GenericTraits {
      public static final String NAME = "stacking";
      public static final StackingTraits INSTANCE = new StackingTraits();
      public static final IEntityTraits<StackingTraits> ENTITY = new IEntityTraits<>() {};
      public static final ITraitCodec<StackingTraits> CODECS = new ITraitCodec<>() {
            @Override
            public Codec<StackingTraits> codec() {
                  return Codec.unit(INSTANCE);
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, StackingTraits> streamCodec() {
                  return StreamCodec.unit(INSTANCE);
            }
      };

      public StackingTraits() {
            super(ModSound.HARD);
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public StackingClient client() {
            return StackingClient.INSTANCE;
      }

      @Override
      public IEntityTraits<StackingTraits> entity() {
            return ENTITY;
      }

      @Override
      public TraitComponentKind<? extends GenericTraits> kind() {
            return Traits.STACKING;
      }

      @Override
      public Fraction fullness(PatchedComponentHolder holder) {
            return null;
      }

      @Override
      public boolean isEmpty(PatchedComponentHolder holder) {
            return false;
      }

      @Override
      public void stackedOnMe(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {

      }

      @Override
      public void stackedOnOther(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir) {

      }

      @Override
      public MutableTraits mutable(PatchedComponentHolder holder) {
            return null;
      }
}
