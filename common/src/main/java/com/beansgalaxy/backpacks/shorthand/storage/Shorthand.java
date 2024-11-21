package com.beansgalaxy.backpacks.shorthand.storage;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.network.clientbound.SendWeaponSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class Shorthand {
      public final ShortContainer tools = new ShortContainer("tools") {
            @Override public int size() {
                  return getToolsSize();
            }
      };

      public final ShortContainer weapons = new ShortContainer("weapons") {
            @Override public int size() {
                  return getWeaponsSize();
            }
      };

      private final Player owner;
      private int timer = 0;
      private int heldSelected = 0;
      private int selectedWeapon = 0;
      private int oToolSize;
      private int oWeaponSize;
      private ItemStack oWeapon;

      public Shorthand(Player player) {
            this.owner = player;
      }

      public static Shorthand get(Inventory that) {
            BackData backData = (BackData) that;
            return backData.getShorthand();
      }

      public static Shorthand get(Player player) {
            BackData backData = BackData.get(player);
            return backData.getShorthand();
      }

      public int size() {
            return tools.getContainerSize() + weapons.getContainerSize();
      }

      public int getSelectedWeapon() {
            return selectedWeapon;
      }

      public void advanceWeapon(Inventory inventory) {
            if (weapons.isEmpty()) {
                  resetSelected(inventory);
                  updateRemoteWeaponSlots(inventory);
                  return;
            }

            int itemsSize = inventory.items.size();
            int toolsSize = tools.getContainerSize();
            int slot = inventory.selected - itemsSize;
            int weaponSlot = slot - toolsSize;
            int weaponsSize = weapons.size();

            int selectedSlot;
            if (weaponsSize > 2) {
                  if (weaponSlot < 0)
                        selectedSlot = selectedWeapon;
                  else {
                        selectedSlot = selectedWeapon + 1 < weaponsSize ? selectedWeapon + 1 : 0;
                  }
            }
            else selectedSlot = 0;

            int i = selectedSlot;
            while (weapons.getItem(i).isEmpty()) {

                  i++;
                  if (i >= weaponsSize)
                        i = 0;

                  if (i == selectedSlot) {
                        resetSelected(inventory);
                        updateRemoteWeaponSlots(inventory);
                        return;
                  }
            }

            int selected = itemsSize + toolsSize + i;
            if (selected == inventory.selected) {
                  resetSelected(inventory);
                  updateRemoteWeaponSlots(inventory);
                  return;
            }

            selectedWeapon = i;
            setHeldSelected(inventory.selected);
            updateRemoteWeaponSlots(inventory);
            inventory.selected = selected;
      }

      public void reduceWeapon(Inventory inventory) {
            if (weapons.isEmpty()) {
                  resetSelected(inventory);
                  updateRemoteWeaponSlots(inventory);
                  return;
            }

            int itemsSize = inventory.items.size();
            int toolsSize = tools.getContainerSize();
            int slot = inventory.selected - itemsSize;
            int weaponSlot = slot - toolsSize;
            int weaponsSize = weapons.size();

            int selectedSlot;
            if (weaponsSize > 2) {
                  if (weaponSlot < 0)
                        selectedSlot = selectedWeapon;
                  else {
                        int i = selectedWeapon > 0 ? selectedWeapon : weaponsSize;
                        selectedSlot = i - 1;
                  }
            }
            else selectedSlot = 1;

            int i = selectedSlot;
            while (weapons.getItem(i).isEmpty()) {

                  i--;
                  if (i < 0)
                        i = weaponsSize - 1;

                  if (i == selectedSlot) {
                        resetSelected(inventory);
                        updateRemoteWeaponSlots(inventory);
                        return;
                  }
            }

            int selected = itemsSize + toolsSize + i;
            if (selected == inventory.selected) {
                  resetSelected(inventory);
                  updateRemoteWeaponSlots(inventory);
                  return;
            }

            selectedWeapon = i;
            setHeldSelected(inventory.selected);
            updateRemoteWeaponSlots(inventory);
            inventory.selected = selected;
      }

      private void updateRemoteWeaponSlots(Inventory inventory) {
            if (inventory.player instanceof ServerPlayer serverPlayer) {
            }
      }

      public ItemStack getItem(int slot) {
            int weaponSlot = slot - tools.getContainerSize();
            return weaponSlot < 0 ? tools.getItem(slot) : weapons.getItem(weaponSlot);
      }

      public int getToolsSize() {
            int maxSlot = 8 - weapons.getContainerSize();
            double attributeValue = owner.getAttributeValue(CommonClass.TOOL_SLOTS_ATTRIBUTE);
            return Math.min((int) attributeValue, maxSlot);
      }

      public int getWeaponsSize() {
            double attributeValue = owner.getAttributeValue(CommonClass.WEAPON_SLOTS_ATTRIBUTE);
            return (int) attributeValue;
      }

      public void dropOverflowItems(ShortContainer container) {
            int maxSlot = container.getMaxSlot();
            int size = container.size();

            if (maxSlot < size)
                  return;

            for (int i = size; i <= maxSlot; i++) {
                  ItemStack removed = container.stacks.remove(i);
                  owner.drop(removed, true);
            }
      }

      public int getQuickestSlot(BlockState blockState) {
            int slot = -1;
            int canadate = -1;
            Inventory inv = owner.getInventory();
            ItemStack mainHandItem = inv.items.get(inv.selected >= inv.items.size() ? heldSelected : inv.selected);
            float topSpeed = mainHandItem.getItem().getDestroySpeed(mainHandItem, blockState);

            boolean requiresToolForDrops = blockState.requiresCorrectToolForDrops();
            for (int i = 0; i < tools.getContainerSize(); i++) {
                  ItemStack tool = tools.getItem(i);
                  float destroySpeed = tool.getItem().getDestroySpeed(tool, blockState);
                  if (destroySpeed > topSpeed) {
                        if (tool.getItem().isCorrectToolForDrops(tool, blockState)) {
                              topSpeed = destroySpeed;
                              slot = i;
                        }
                        else if (!requiresToolForDrops) 
                              canadate = i;
                  }
            }

            return slot == -1 ? canadate : slot;
      }

      private float getBlockHardness(BlockState blockState, BlockPos pos) {
            Level level = owner.level();
            BlockGetter chunkForCollisions = level.getChunkForCollisions(
                        SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));

            if (chunkForCollisions == null) return 1f;

            return blockState.getDestroySpeed(chunkForCollisions, pos);
      }

      public void onAttackBlock(BlockState blockState, float blockHardness) {
            Inventory inv = owner.getInventory();
            int weaponSlot = inv.selected - inv.items.size() - tools.getContainerSize();
            if (weaponSlot >= 0)
                  return;

            if (blockHardness < 0.1f)
                  resetSelected(inv);
            else {
                  int slot = this.getQuickestSlot(blockState);
                  if (slot > -1) {
                        setTimer(25);
                        setHeldSelected(inv.selected);
                        int newSelected = slot + inv.items.size();
                        if (inv.selected != newSelected)
                              inv.selected = newSelected;
                  } else
                        resetSelected(inv);
            }
      }

      private void setHeldSelected(int selected) {
            if (selected < 9)
                  heldSelected = selected;
      }

      private void setTimer(int time) {
            this.timer = time;
      }

      public void tick() {
            if (oToolSize > getToolsSize())
                  dropOverflowItems(tools);

            if (oWeaponSize > getWeaponsSize())
                  dropOverflowItems(weapons);

            if (owner instanceof ServerPlayer serverPlayer) {
                  int selectedWeapon = getSelectedWeapon();
                  Inventory inventory = serverPlayer.getInventory();
                  int selected = inventory.selected - inventory.items.size() - tools.getContainerSize();
                  ItemStack weapon;
                  if (selected == selectedWeapon)
                        weapon = ItemStack.EMPTY;
                  else if (selected >= 0) {
                        selectedWeapon = selected;
                        this.selectedWeapon = selectedWeapon;
                        weapon = ItemStack.EMPTY;
                  } else
                        weapon = weapons.getItem(selectedWeapon);

                  if (oWeapon != weapon) {
                        oWeapon = weapon;
                        SendWeaponSlot.send(serverPlayer, selectedWeapon, weapon);
                  }
            }

            oToolSize = getToolsSize();
            oWeaponSize = getWeaponsSize();

            Inventory inventory = owner.getInventory();
            int slot = inventory.selected - inventory.items.size();
            if (size() <= slot || slot < 0) {
                  if (inventory.selected >= inventory.items.size())
                        inventory.selected = heldSelected;
                  timer = 0;
                  return;
            }

            boolean isToolSelected = slot < tools.getContainerSize();
            if (!isToolSelected) {
                  timer = 0;
                  return;
            }

            if (timer == 1)
                  resetSelected(inventory);

            if (timer > 0) {
                  if (tools.getItem(slot).isEmpty())
                        resetSelected(inventory);
                  timer--;
            }
      }

      public void resetSelected(Inventory inventory) {
            if (inventory.selected >= inventory.items.size())
                  inventory.selected = heldSelected;
      }

      public CompoundTag save(CompoundTag tag, RegistryAccess access) {
            CompoundTag shorthand = new CompoundTag();
            tools.save(shorthand, access);
            weapons.save(shorthand, access);
            tag.put("Shorthand", shorthand);
            return tag;
      }

      public void load(CompoundTag tag, RegistryAccess access) {
            CompoundTag shorthand = tag.getCompound("Shorthand");
            tools.load(shorthand, access);
            weapons.load(shorthand, access);
      }

      public int getSelected(Inventory instance) {
            int slot = instance.selected - instance.items.size();
            if (size() > slot && slot >= 0)
                  return slot;
            else {
                  resetSelected(instance);
                  return -1;
            }
      }

      public void replaceWith(Shorthand that) {
            this.weapons.clearContent();
            that.weapons.stacks.forEach((i, stack) -> {
                  if (!stack.isEmpty())
                        this.weapons.stacks.put(i, stack);
            });

            this.tools.clearContent();
            that.tools.stacks.forEach((i, stack) -> {
                  if (!stack.isEmpty())
                        this.tools.stacks.put(i, stack);
            });

            ItemStack backpack = that.owner.getItemBySlot(EquipmentSlot.BODY);
            this.owner.setItemSlot(EquipmentSlot.BODY, backpack);
      }

      public void updateSelectedWeapon(int selectedSlot, ItemStack stack) {
            selectedWeapon = selectedSlot;
            weapons.setItem(selectedSlot, stack);
      }
}
