package com.beansgalaxy.backpacks.traits.generic;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

public abstract class GenericTraits {
      private final ResourceLocation location;
      private final ModSound sound;

      public GenericTraits(ResourceLocation location, ModSound sound) {
            this.location = location;
            this.sound = sound;
      }

      public static void encodeLocation(RegistryFriendlyByteBuf buf, GenericTraits fields) {
            fields.location().ifPresentOrElse(location -> {
                  buf.writeBoolean(true);
                  ResourceLocation.STREAM_CODEC.encode(buf, location);
            }, () -> buf.writeBoolean(false));
      }

      @Nullable
      public static ResourceLocation decodeLocation(RegistryFriendlyByteBuf buf) {
            if (buf.readBoolean())
                  return ResourceLocation.STREAM_CODEC.decode(buf);

            return null;
      }

      public abstract String name();

      public abstract IClientTraits client();

      abstract public <T extends GenericTraits> T toReference(ResourceLocation location);

      public abstract Fraction fullness(PatchedComponentHolder holder);

      public Fraction fullness(ItemStack stack) {
            return fullness(PatchedComponentHolder.of(stack));
      }

      public boolean isFull(ItemStack stack) {
            return isFull(PatchedComponentHolder.of(stack));
      }

      public boolean isFull(PatchedComponentHolder holder) {
            Fraction fullness = fullness(holder);
            int i = fullness.compareTo(Fraction.ONE);
            return i >= 0;
      }

      public boolean isEmpty(ItemStack stack) {
            return isEmpty(PatchedComponentHolder.of(stack));
      }

      public abstract boolean isEmpty(PatchedComponentHolder holder);

      public abstract void stackedOnMe(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir);

      public abstract void stackedOnOther(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir);

      public void useOn(UseOnContext ctx, ItemStack backpack, CallbackInfoReturnable<InteractionResult> cir) {

      }

      public void use(Level level, Player player, InteractionHand hand, ItemStack backpack, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {

      }

      public void inventoryTick(PatchedComponentHolder backpack, Level level, Entity entity, int slot, boolean selected) {

      }

      public abstract MutableTraits newMutable(PatchedComponentHolder holder);

      public boolean isStackable(PatchedComponentHolder holder) {
            return false;
      }

      public ModSound sound() {
            return sound;
      }

      abstract public TraitComponentKind<? extends GenericTraits> kind();

      public Optional<ResourceLocation> location() {
            return Optional.ofNullable(location);
      }
}
