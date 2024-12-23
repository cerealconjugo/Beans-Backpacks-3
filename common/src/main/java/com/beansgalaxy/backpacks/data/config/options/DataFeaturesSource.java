package com.beansgalaxy.backpacks.data.config.options;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.data.config.FeaturesConfig;
import com.beansgalaxy.backpacks.data.config.types.MapConfigVariant;
import com.beansgalaxy.backpacks.platform.Services;
import net.minecraft.FileUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.level.validation.DirectoryValidator;
import org.apache.commons.compress.utils.Lists;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

public class DataFeaturesSource implements RepositorySource {

      private static final PackSelectionConfig DISCOVERED_PACK_SELECTION_CONFIG =
                  new PackSelectionConfig(true, Pack.Position.TOP, true);
      private final Path folder;
      private final DirectoryValidator validator;

      public DataFeaturesSource(Path pFolder, DirectoryValidator pValidator) {
            this.folder = pFolder;
            this.validator = pValidator;
      }

      public static Path getPath() {
            return Services.PLATFORM.getConfigDir().resolve(Constants.MOD_ID + "/enabled_features");
      }

      public static Path getDisabledPath() {
            return Services.PLATFORM.getConfigDir().resolve(Constants.MOD_ID + "/disabled_features");
      }

      public static DataFeaturesSource createRepositorySource(Path path, DirectoryValidator validator) {
            return new DataFeaturesSource(path, validator);
      }

      private static String nameFromPath(Path pPath) {
            return pPath.getFileName().toString();
      }

      private PackLocationInfo createDiscoveredFilePackInfo(Path pPath) {
            String s = nameFromPath(pPath);
            return new PackLocationInfo("backpacks/" + s, Component.literal(s), PackSource.WORLD, Optional.empty());
      }


      public void loadPacks(Consumer<Pack> pOnLoad) {
            try {
                  FileUtil.createDirectoriesSafe(this.folder);
                  ArrayList<Pack> packs = new ArrayList<>();
                  FeaturesConfig config = new FeaturesConfig();
                  config.read(false);

                  FolderRepositorySource.discoverPacks(this.folder, this.validator, (p_325639_, p_325640_) -> {
                        PackLocationInfo packlocationinfo = this.createDiscoveredFilePackInfo(p_325639_);
                        Pack pack = Pack.readMetaAndCreate(packlocationinfo, p_325640_, PackType.SERVER_DATA, DISCOVERED_PACK_SELECTION_CONFIG);
                        if (pack != null)
                              packs.add(pack);
                  });

                  packs.forEach(pOnLoad);

            } catch (IOException var3) {
                  IOException ioexception = var3;
                  Constants.LOG.warn("Failed to list packs in {}", this.folder, ioexception);
            }
      }

      public static int compare(Pack pack1, Pack pack2, FeaturesConfig config) {
            return pack1.getId().compareTo(pack2.getId());
      }

      private static int getPackPriority(MapConfigVariant<String, Integer> feature_priority, Pack pack) {
            return feature_priority.get().getOrDefault(pack.getId(), 0);
      }
}
