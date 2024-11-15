package com.beansgalaxy.backpacks.traits.bulk;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.IEntityTraits;
import com.beansgalaxy.backpacks.traits.generic.BackpackEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class BulkEntity implements IEntityTraits<BulkTraits> {
      public static final BulkEntity INSTANCE = new BulkEntity();

      @Override
      public InteractionResult interact(BackpackEntity backpack, BulkTraits traits, Player player, InteractionHand hand) {
            BulkMutable bulkMutable = traits.mutable(backpack);
            ItemStack itemInHand = player.getItemInHand(hand);
            if (itemInHand.isEmpty()) {
                  if (!bulkMutable.isEmpty() && findAndInsertFromInventory(player, bulkMutable)) {
                        bulkMutable.push();
                        return InteractionResult.SUCCESS;
                  }

                  return IEntityTraits.super.interact(backpack, traits, player, hand);
            }

            if (bulkMutable.isFull()) {
                  if (!backpack.isSilent()) {
                        backpack.wobble(10);
                        float pitch = backpack.getRandom().nextFloat() * 0.3f;
                        traits.sound().at(backpack, ModSound.Type.HIT, 0.8f, pitch + 0.9f);
                        return InteractionResult.SUCCESS;
                  }
            }
            ItemStack stack = bulkMutable.addItem(itemInHand, player);
            if (stack != null) {
                  bulkMutable.push();
                  if (!backpack.isSilent()) {
                        traits.sound().at(backpack, ModSound.Type.INSERT);
                  }
                  return InteractionResult.SUCCESS;
            }
            else if (!bulkMutable.isEmpty() && findAndInsertFromInventory(player, bulkMutable)) {
                  bulkMutable.push();
                  return InteractionResult.SUCCESS;
            }
            return IEntityTraits.super.interact(backpack, traits, player, hand);
      }

      private boolean findAndInsertFromInventory(Player player, BulkMutable bulkMutable) {
            Holder<Item> itemHolder = bulkMutable.bulkList.get().itemHolder();
            Inventory inventory = player.getInventory();
            for (ItemStack item : inventory.items) {
                  if (item.is(itemHolder) && bulkMutable.addItem(item, player) != null)
                        return true;
            }
            return false;
      }

      @Override
      public void onDamage(BackpackEntity backpack, BulkTraits traits, boolean silent, ModSound sound) {
            BulkMutable mutable = traits.mutable(backpack);
            ItemStack stack = mutable.removeItem(0);
            if (!stack.isEmpty()) {
                  backpack.wobble(10);
                  Level level = backpack.level();
                  if (!level.isClientSide) {
                        Direction direction = backpack.getDirection();
                        Vector3f step;
                        if (direction.getAxis().isVertical())
                              step = new Vector3f(0, 9/16f, 0);
                        else
                              step = direction.step().mul(0.3f);
                        Vec3 pos = backpack.position();
                        ItemEntity itemEntity = new ItemEntity(level, pos.x + step.x, pos.y + step.y, pos.z + step.z, stack);
                        itemEntity.setDeltaMovement(0, 0.15, 0);
                        itemEntity.setDefaultPickUpDelay();
                        level.addFreshEntity(itemEntity);
                  }
                  mutable.push();
                  if (!backpack.isSilent()) {
                        traits.sound().at(backpack, ModSound.Type.REMOVE);
                  }
                  return;
            } else if (backpack.wobble > 0) {
                  backpack.wobble(10);
                  if (!silent) {
                        float pitch = backpack.getRandom().nextFloat() * 0.3f;
                        sound.at(backpack, ModSound.Type.HIT, 1f, pitch + 0.9f);
                  }
                  return;
            }

            IEntityTraits.super.onDamage(backpack, traits, silent, sound);
      }

      @Override
      public Container createHopperContainer(BackpackEntity backpack, BulkTraits traits) {
            return new BulkHopper(backpack, traits);
      }
}
