package com.beansgalaxy.backpacks.trait.battery;

import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.generic.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.Objects;

public class BatteryTraits implements GenericTraits {
      public static final String NAME = "battery";
      private final BatteryFields fields;
      private final ItemStack stack;
      private final int amount;

      public BatteryTraits(BatteryFields fields, ItemStack stack, int amount) {
            this.fields = fields;
            this.stack = stack;
            this.amount = amount;
      }

      @Override
      public BatteryFields fields() {
            return fields;
      }

      public ItemStack stack() {
            return stack;
      }

      public int amount() {
            return amount;
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public IClientTraits client() {
            return BatteryClient.INSTANCE;
      }

      @Override
      public BatteryTraits toReference(ResourceLocation location) {
            return new BatteryTraits(fields.toReference(location), stack, amount);
      }

      @Override
      public int size() {
            return fields().size();
      }

      public long speed() {
            return fields.speed();
      }

      @Override
      public Fraction fullness() {
            return Fraction.getFraction(amount, fields().size());
      }

      @Override
      public void stackedOnMe(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {
            ItemMutable mutable = mutable();
            if (EquipableComponent.canEquip(backpack, slot)) {
                  ItemStack itemStack = mutable.insert(other, player);
                  access.set(itemStack);
                  freezeAndCancel(backpack, mutable, cir);
            }
            else if (ClickAction.SECONDARY.equals(click)) {
                  ItemStack itemStack = mutable.insert(other, player);
                  access.set(itemStack);
                  freezeAndCancel(backpack, mutable, cir);
            }
      }

      @Override
      public void stackedOnOther(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir) {
            if (ClickAction.SECONDARY.equals(click) && EquipableComponent.get(backpack).isEmpty()) {
                  ItemMutable mutable = mutable();
                  ItemStack itemStack = mutable.insert(other, player);
                  slot.set(itemStack);

                  freezeAndCancel(backpack, mutable, cir);
            }
      }

      @Override
      public void inventoryTick(PatchedComponentHolder backpack, Level level, Entity entity, int slot, boolean selected) {
            ContainerItemContext context = ContainerItemContext.withConstant(stack);
            EnergyStorage to = EnergyStorage.ITEM.find(stack, context);
            if (to == null) return;

            EnergyMutable mutable = energyMutable(backpack);
            try(Transaction transaction = Transaction.openOuter()) {
                  EnergyStorageUtil.move(mutable, to, fields.speed(), transaction);
                  transaction.commit();
            }
      }

      private void freezeAndCancel(PatchedComponentHolder backpack, ItemMutable mutable, CallbackInfoReturnable<Boolean> cir) {
            cir.setReturnValue(true);
            kind().freezeAndCancel(backpack, mutable);
      }

      @Override
      public boolean isEmpty() {
            return stack.isEmpty() && amount == 0;
      }

      public EnergyMutable energyMutable(PatchedComponentHolder backpack) {
            return new EnergyMutable(backpack, fields);
      }

      @Override
      public ItemMutable mutable() {
            return new ItemMutable();
      }

      public class EnergyMutable extends SimpleEnergyStorage {
            private final PatchedComponentHolder backpack;
            private final ItemStack stack;

            public EnergyMutable(PatchedComponentHolder backpack, BatteryFields fields) {
                  super(fields.size(), fields.speed(), fields.speed());
                  this.amount = BatteryTraits.this.amount;
                  this.backpack = backpack;
                  this.stack = BatteryTraits.this.stack;
            }

            public BatteryTraits freeze() {
                  return new BatteryTraits(BatteryTraits.this.fields, stack, (int) Math.min(this.amount, Integer.MAX_VALUE));
            }

            @Override
            protected void onFinalCommit() {
                  BatteryTraits freeze = freeze();
                  kind().save(backpack, freeze);
            }
      }

      public class ItemMutable implements MutableTraits {
            @NotNull
            private ItemStack stack;

            public ItemMutable() {
                  this.stack = BatteryTraits.this.stack;
            }

            @Override
            public BatteryTraits freeze() {
                  return new BatteryTraits(BatteryTraits.this.fields, stack, BatteryTraits.this.amount);
            }

            @Override @Nullable
            public ItemStack addItem(ItemStack stack, Player player) {
                  if (!stack.isEmpty() && this.stack.isEmpty()) {
                        this.stack = stack.split(1);
                        return stack;
                  }
                  return null;
            }

            @Override @NotNull
            public ItemStack removeItemNoUpdate(ItemStack carried, Player player) {
                  if (carried.getCount() == 1 || carried.isEmpty()) {
                        ItemStack returnStack = this.stack;
                        this.stack = carried;
                        return returnStack;
                  }
                  return carried;
            }

            public ItemStack insert(ItemStack other, Player player) {
                  ItemStack stack = addItem(other, player);
                  if (stack == null) {
                        stack = removeItemNoUpdate(other, player);
                        sound().atClient(player, ModSound.Type.REMOVE);
                  } else {
                        sound().atClient(player, ModSound.Type.INSERT);
                  }

                  return stack;
            }

            @Override
            public void dropItems(Entity backpackEntity) {

            }

            @Override
            public InteractionResult interact(BackpackEntity backpackEntity, Player player, InteractionHand hand) {
                  return InteractionResult.SUCCESS;
            }

            @Override
            public GenericTraits trait() {
                  return BatteryTraits.this;
            }
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BatteryTraits that)) return false;
            return amount == that.amount && Objects.equals(fields, that.fields) && Objects.equals(stack, that.stack);
      }

      @Override
      public int hashCode() {
            return Objects.hash(fields, stack, amount);
      }

      @Override
      public String toString() {
            return "BatteryTraits{" +
                        "fields=" + fields +
                        ", stack=" + stack +
                        ", amount=" + amount +
                        '}';
      }
}
