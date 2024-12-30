package com.beansgalaxy.backpacks.data.config.options;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.*;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public interface DataFeaturesSource {

      static Path getPath() {
            return Services.PLATFORM.getConfigDir().resolve(Constants.MOD_ID + "/features");
      }

      UnaryOperator<Component> PACK_DISPLAY = component -> Component.translatable("pack.nameAndSource", component, Component.translatable("pack.source.beansbackpacks")).withStyle(ChatFormatting.GRAY);
      PackSource DATA_ENABLED = PackSource.create(PACK_DISPLAY, true);

      static void addPacks(Consumer<Pack> addPack) {
            Optional<Path> optional = Services.PLATFORM.getModFeaturesDir();
            optional.ifPresent(path ->
                  addPacks(addPack, "built-in/", path)
            );

            Path features = DataFeaturesSource.getPath();
            if (!addPacks(addPack, "feature/", features)) {

                  try {
                        Files.createDirectories(features);
                  } catch (IOException e) {
                        throw new RuntimeException(e);
                  }
            }
      }

      private static boolean addPacks(Consumer<Pack> addPack, String prefix, Path path) {
            if (Files.notExists(path))
                  return false;

            try (DirectoryStream<Path> paths = Files.newDirectoryStream(path)) {
                  for (Path next : paths) {
                        addPack(addPack, prefix, next);
                  }
            } catch (IOException e) {
                  throw new RuntimeException(e);
            }
            return true;
      }

      private static void addPack(Consumer<Pack> addPack, String prefix, Path next) {
            Path name = next.getFileName();
            String id = prefix + name;

            KnownPack knownPack = new KnownPack(Constants.MOD_ID, id, "0.0");
            PackLocationInfo info = new PackLocationInfo(
                        id,
                        Component.literal(String.valueOf(name)),
                        DATA_ENABLED,
                        Optional.of(knownPack)
            );

            Pack.ResourcesSupplier supplier = new Pack.ResourcesSupplier() {
                  public PackResources openPrimary(PackLocationInfo info) {
                        return new PathPackResources(info, next);
                  }

                  public PackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
                        return new PathPackResources(info, next);
                  }
            };

            Pack pack = Pack.readMetaAndCreate(
                        info,
                        supplier,
                        PackType.SERVER_DATA,
                        new PackSelectionConfig(true, Pack.Position.TOP, false)
            );

            if (pack != null)
                  addPack.accept(pack);
      }
}
