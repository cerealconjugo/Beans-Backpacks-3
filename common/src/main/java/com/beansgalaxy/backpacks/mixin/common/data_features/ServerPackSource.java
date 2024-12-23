package com.beansgalaxy.backpacks.mixin.common.data_features;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.data.config.options.DataFeaturesSource;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.packs.repository.*;
import net.minecraft.world.level.validation.DirectoryValidator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(ServerPacksSource.class)
public class ServerPackSource {
      @Redirect(method = "createPackRepository(Ljava/nio/file/Path;Lnet/minecraft/world/level/validation/DirectoryValidator;)Lnet/minecraft/server/packs/repository/PackRepository;",
                  at = @At(value = "NEW", target = "([Lnet/minecraft/server/packs/repository/RepositorySource;)Lnet/minecraft/server/packs/repository/PackRepository;"))
      private static PackRepository backpacks_injectDataFeatures(RepositorySource[] o, @Local(argsOnly = true) DirectoryValidator pValidator) {
            Path featuresPath = DataFeaturesSource.getPath();

            if (Files.notExists(featuresPath)) {
                  try {
                        Files.createDirectory(featuresPath);
                  } catch (IOException ignored) {
                        Constants.LOG.warn("Failed to create Data Features Directory at; {}", featuresPath);
                  }
            }

            RepositorySource[] sources = new RepositorySource[o.length + 1];
            RepositorySource featureSource = DataFeaturesSource.createRepositorySource(featuresPath, pValidator);

            int i = 0;
            sources[i] = featureSource;
            for (RepositorySource source : o) {
                  i++;
                  sources[i] = source;
            }

            return new PackRepository(sources);
      }
}
