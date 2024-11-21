package com.beansgalaxy.backpacks.data;

import com.beansgalaxy.backpacks.Constants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class ServerSave extends SavedData {
      public final EnderStorage enderStorage = new EnderStorage();

      @Override
      public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
            enderStorage.save(tag);
            return tag;
      }

      private static ServerSave load(CompoundTag tag, HolderLookup.Provider provider) {
            ServerSave save = new ServerSave();
            save.enderStorage.load(tag);
            return save;
      }

      public static SavedData.Factory<ServerSave> factory() {
            return new SavedData.Factory<>(ServerSave::new, ServerSave::load, DataFixTypes.LEVEL);
      }

      public static ServerSave getSave(MinecraftServer server, boolean updateSave) {
            ServerLevel level = server.getLevel(Level.OVERWORLD);
            DimensionDataStorage dataStorage = level.getDataStorage();
            ServerSave save = dataStorage.computeIfAbsent(ServerSave.factory(), Constants.MOD_ID);

            if (updateSave)
                  save.setDirty();

            return save;
      }
}
