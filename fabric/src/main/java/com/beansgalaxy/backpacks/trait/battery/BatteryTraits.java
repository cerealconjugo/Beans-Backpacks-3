package com.beansgalaxy.backpacks.trait.battery;

import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.*;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.Objects;

public class BatteryTraits extends GenericTraits {
      public static final String NAME = "battery";
      private final long capacity;
      private final long speed;

      public BatteryTraits(@Nullable ResourceLocation location, ModSound sound, long capacity, long speed) {
            super(location, sound);
            this.capacity = capacity;
            this.speed = speed;
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public BatteryClient client() {
            return BatteryClient.INSTANCE;
      }

      @Override
      public BatteryEntity entity() {
            return BatteryEntity.INSTANCE;
      }

      @Override
      public BatteryTraits toReference(ResourceLocation location) {
            return new BatteryTraits(location, sound(), capacity, speed);
      }

      public long size() {
            return capacity;
      }

      public long speed() {
            return speed;
      }

      @Override
      public Fraction fullness(PatchedComponentHolder holder) {
            Long amount = holder.get(ITraitData.LONG);
            if (amount == null)
                  return Fraction.ZERO;

            return Fraction.getFraction(amount.intValue(), (int) size());
      }

      @Override
      public void stackedOnMe(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {
            BatteryMutable mutable = newMutable(backpack);
            if (mutable.item.get().isEmpty() && other.isEmpty()) {
                  return;
            }

            boolean empty = !EquipableComponent.testIfPresent(backpack, equipable -> !equipable.traitRemovable());
            if (empty) {
                  if (ClickAction.SECONDARY.equals(click)) {
                        ItemStack itemStack = mutable.insert(other, player);
                        access.set(itemStack);
                        mutable.push(cir);
                  }
            }
            else if (EquipableComponent.canEquip(backpack, slot)) {
                  ItemStack itemStack = mutable.insert(other, player);
                  access.set(itemStack);
                  mutable.push(cir);
            }
      }

      @Override
      public void stackedOnOther(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir) {
            if (ClickAction.SECONDARY.equals(click) && EquipableComponent.get(backpack).isEmpty()) {
                  BatteryMutable mutable = newMutable(backpack);
                  ItemStack itemStack = mutable.insert(other, player);
                  slot.set(itemStack);
                  mutable.push(cir);
            }
      }

      @Override
      public void inventoryTick(PatchedComponentHolder backpack, Level level, Entity entity, int slot, boolean selected) {
            ItemStack stack = backpack.get(ITraitData.SOLO_STACK);
            if (stack == null || stack.isEmpty())
                  return;

            ContainerItemContext context = ContainerItemContext.withConstant(stack);
            EnergyStorage to = EnergyStorage.ITEM.find(stack, context);
            if (to == null) return;

            BatteryMutable mutable = newMutable(backpack);
            SimpleEnergyStorage storage = mutable.getStorage();
            try(Transaction transaction = Transaction.openOuter()) {
                  EnergyStorageUtil.move(storage, to, speed(), transaction);
                  transaction.commit();
            }
      }

      @Override
      public BatteryMutable newMutable(PatchedComponentHolder holder) {
            return new BatteryMutable(this, holder);
      }

      @Override
      public boolean isEmpty(PatchedComponentHolder holder) {
            return !holder.has(ITraitData.SOLO_STACK);
      }

      @Override
      public TraitComponentKind<? extends GenericTraits> kind() {
            return Traits.BATTERY;
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BatteryTraits that)) return false;
            return size() == that.size() && Objects.equals(sound(), that.sound()) && Objects.equals(location(), that.location());
      }

      @Override
      public int hashCode() {
            return Objects.hash(size(), sound(), location());
      }

      @Override
      public String toString() {
            return "BatteryTraits{" +
                        "size=" + size() +
                        "sound=" + sound() +
                        location().map(
                                                location -> "location=" + location + '}')
                                    .orElse("}");
      }
}
