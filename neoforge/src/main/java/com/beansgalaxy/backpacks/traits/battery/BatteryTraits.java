package com.beansgalaxy.backpacks.traits.battery;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.*;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.MutableTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.energy.ComponentEnergyStorage;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.Objects;

public class BatteryTraits extends GenericTraits {
      public static final String NAME = "battery";
      private final long size;
      private final long speed;

      public BatteryTraits(ResourceLocation location, ModSound sound, long size, long speed) {
            super(location, sound);
            this.size = size;
            this.speed = speed;
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public IClientTraits<? extends GenericTraits> client() {
            return null;
      }

      @Override
      public BatteryEntity entity() {
            return BatteryEntity.INSTANCE;
      }

      @Override
      public BatteryTraits toReference(ResourceLocation location) {
            return new BatteryTraits(location, sound(), size, speed);
      }

      public long size() {
            return size;
      }

      @Override
      public Fraction fullness(PatchedComponentHolder holder) {
            Long aLong = holder.get(ITraitData.LONG);
            if (aLong == null || aLong == 0)
                  return Fraction.ZERO;
            return Fraction.getFraction(aLong.intValue(), ((int) size()));
      }

      @Override
      public boolean isEmpty(PatchedComponentHolder holder) {
            return holder.has(ITraitData.LONG) || holder.has(ITraitData.SOLO_STACK);
      }

      @Override
      public void stackedOnMe(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {

      }

      @Override
      public void stackedOnOther(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir) {

      }

      @Override
      public void inventoryTick(PatchedComponentHolder backpack, Level level, Entity entity, int slot, boolean selected) {
            ItemStack stack = backpack.get(ITraitData.SOLO_STACK);
            if (stack == null || stack.isEmpty())
                  return;

            Iterator<TypedDataComponent<?>> iterator = stack.getComponents().iterator();
            TypedDataComponent<? extends ComponentEnergyStorage> typedData = null;
            while (iterator.hasNext() && typedData == null) {
                  TypedDataComponent<?> next = iterator.next();
                  if (next.value() instanceof ComponentEnergyStorage componentEnergyStorage) {
                        typedData = (TypedDataComponent<? extends ComponentEnergyStorage>) next;
                  }
            }
            
            if (typedData == null) 
                  return;

            DataComponentMap components = stack.getComponents();
            if (components instanceof PatchedDataComponentMap map) {
                  typedData.applyTo(map);
            }
      }

      @Override
      public MutableTraits newMutable(PatchedComponentHolder holder) {
            return null;
      }

      @Override
      public TraitComponentKind<? extends GenericTraits> kind() {
            return Traits.BATTERY;
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BatteryTraits that)) return false;
            if (!super.equals(o)) return false;
            return size == that.size && speed == that.speed
                        && Objects.equals(location(), that.location())
                        && sound() == that.sound();
      }

      @Override
      public int hashCode() {
            return Objects.hash(super.hashCode(), size, speed);
      }

      @Override
      public String toString() {
            return "BatteryFields{" +
                        "size=" + size +
                        ", speed=" + speed +
                        ", sound=" + sound() +
                        location().map(location ->
                                                ", location=" + location + '}')
                                    .orElse("}"
                                    );
      }
}
