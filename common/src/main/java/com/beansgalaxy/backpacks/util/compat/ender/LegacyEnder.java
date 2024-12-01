package com.beansgalaxy.backpacks.util.compat.ender;

import com.beansgalaxy.backpacks.util.compat.BPack2PlayerFix;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class LegacyEnder {
      public final HashMap<UUID, List<ItemStack>> MAP = new HashMap<>();

      public void fromNbt(CompoundTag tag) {
            if (tag.contains("EnderData")) {
                  CompoundTag entries = tag.getCompound("EnderData");
                  for (String key : entries.getAllKeys()) {
                        UUID uuid = UUID.fromString(key);

                        CompoundTag dataTags = entries.getCompound(key);
                        List<ItemStack> stacks = readStackNbt(dataTags);
                        MAP.put(uuid, stacks);
                  }
                  MAP.remove(null);
            }
      }

      public static List<ItemStack> readStackNbt(CompoundTag nbt) {
            ListTag nbtList = nbt.getList("Items", Tag.TAG_COMPOUND);
            Dynamic<Tag> dynamic = new Dynamic<>(NbtOps.INSTANCE, nbtList);
            Dynamic<Tag> updated = BPack2PlayerFix.updateVersion(dynamic);
            DataResult<List<ItemStack>> result = ItemStack.CODEC.listOf().parse(updated);
            if (result.isError())
                  return List.of();

            return result.getOrThrow();
      }
}
