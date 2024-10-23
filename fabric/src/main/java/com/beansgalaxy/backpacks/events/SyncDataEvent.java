package com.beansgalaxy.backpacks.events;

import com.beansgalaxy.backpacks.network.clientbound.ConfigureReferences;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.level.ServerPlayer;

public class SyncDataEvent implements ServerLifecycleEvents.SyncDataPackContents {
      @Override
      public void onSyncDataPackContents(ServerPlayer player, boolean joined) {
            ConfigureReferences.send(player);
      }
}
