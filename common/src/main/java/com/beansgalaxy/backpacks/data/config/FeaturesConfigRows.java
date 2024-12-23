package com.beansgalaxy.backpacks.data.config;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.data.config.options.DataFeaturesSource;
import com.beansgalaxy.backpacks.data.config.screen.ConfigRows;
import com.beansgalaxy.backpacks.data.config.screen.ConfigScreen;
import com.beansgalaxy.backpacks.data.config.types.ListConfigVariant;
import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.compress.utils.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class FeaturesConfigRows extends ConfigRows {
      private static final ResourceLocation DEFAULT_ICON = ResourceLocation.withDefaultNamespace("textures/misc/unknown_pack.png");
      private final List<ConfigLabel> rows;

      private final List<FeatureEntry> enabled_features;
      private final List<FeatureEntry> disabled_features;

      public FeaturesConfigRows(ConfigScreen screen, Minecraft minecraft, FeaturesConfig config) {
            super(screen, minecraft, config);

            this.rows = getRows();
            for (ConfigLabel row : rows)
                  addEntry(row);

            List<FeatureEntry> enabled = new ArrayList<>();
            List<FeatureEntry> disabled = new ArrayList<>();
            Path path = DataFeaturesSource.getPath();
            RepositorySource source = DataFeaturesSource.createRepositorySource(path, minecraft.directoryValidator());

            source.loadPacks(pack -> {
                  ResourceLocation icon = loadPackIcon(minecraft.getTextureManager(), pack);
                  FeatureEntry entry = new FeatureEntry(pack, icon);
                  enabled.add(entry);
                  entry.enabled = true;
            });

            Path disabledPath = DataFeaturesSource.getDisabledPath();
            RepositorySource disabledSource = DataFeaturesSource.createRepositorySource(disabledPath, minecraft.directoryValidator());
            disabledSource.loadPacks(pack -> {
                  ResourceLocation icon = loadPackIcon(minecraft.getTextureManager(), pack);
                  FeatureEntry entry = new FeatureEntry(pack, icon);
                  disabled.add(entry);
            });

            this.enabled_features = enabled;
            this.disabled_features = disabled;
      }

      private List<ConfigLabel> getRows() {
            FeaturesConfig config = (FeaturesConfig) this.config;
            return List.of(
                        new ConfigDescription(Component.translatable("config.beansbackpacks.features.description")),
                        new ConfigLabel(Component.translatable("config.beansbackpacks.features.enabled-packs")),
                        new EnabledFeaturesRow(config),
                        new ConfigLabel(Component.translatable("config.beansbackpacks.features.disabled-packs")),
                        new DisabledFeaturesRow(config)
            );
      }

      @Override
      public void resetToDefault() {
            for (ConfigLabel row : rows)
                  row.resetToDefault();
      }

      @Override public void onSave() {
            ArrayList<String> packs = new ArrayList<>();
            Path featuresPath = DataFeaturesSource.getPath();
            Path disabledPath = DataFeaturesSource.getDisabledPath();
            try {
                  if (Files.notExists(featuresPath))
                        Files.createDirectory(featuresPath);

                  if (Files.notExists(disabledPath))
                        Files.createDirectory(disabledPath);
            }
            catch (IOException e) {
                  throw new RuntimeException(e);
            }

            for (FeatureEntry entry : enabled_features) {
                  String id = entry.pack.getId();
                  String fileName = id.replaceFirst("backpacks/", "");
                  Path filePath = disabledPath.resolve(fileName);
                  if (Files.exists(filePath))
                        try {
                              Path targetPath = featuresPath.resolve(fileName);
                              Files.move(filePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                              Constants.LOG.warn("Error saving features config", e);
                        }

                  packs.add(id);
            }


            for (FeatureEntry entry : disabled_features) {
                  String id = entry.pack.getId();
                  String fileName = id.replaceFirst("backpacks/", "");
                  Path filePath = featuresPath.resolve(fileName);
                  if (Files.exists(filePath))
                        try {
                              Path targetPath = disabledPath.resolve(fileName);
                              Files.move(filePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                              Constants.LOG.warn("Error saving features config", e);
                        }
            }
      }

      private final class EnabledFeaturesRow extends FeatureSelectionRow {
            public EnabledFeaturesRow(FeaturesConfig config) {
                  super(Component.literal("enabled_data_features"));
            }

            @Override
            public List<FeatureEntry> features() {
                  return enabled_features;
            }
      }

      private final class DisabledFeaturesRow extends FeatureSelectionRow {
            public DisabledFeaturesRow(FeaturesConfig config) {
                  super(Component.literal("disabled_data_features"));
            }

            @Override
            public List<FeatureEntry> features() {
                  return disabled_features;
            }
      }

      private abstract class FeatureSelectionRow extends ConfigLabel {

            public FeatureSelectionRow(Component name) {
                  super(name);
            }

            @Override
            public void render(GuiGraphics gui, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
                  final int[] topPos = {0};
                  features().stream().sorted().forEach(feature -> {
                        feature.setX(x);
                        feature.setY(topPos[0] + y);
                        feature.render(gui, mouseX, mouseY, delta);
                        topPos[0] += 40;
                  });
            }

            public abstract List<FeatureEntry> features();

            @Override
            public int getHeight() {
                  return features().size() * 40;
            }

            @Override
            public List<? extends GuiEventListener> children() {
                  return features().stream().toList();
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                  return features().stream().toList();
            }
      }

       class FeatureEntry extends Button implements Comparable<FeatureEntry> {
            private final Pack pack;
            private final ResourceLocation icon;
            boolean enabled = false;

            FeatureEntry(Pack pack, ResourceLocation icon) {
                  super(0, 0, 32, 32, pack.getTitle(), b -> {}, DEFAULT_NARRATION);
                  this.pack = pack;
                  this.icon = icon;
            }

            @Override
            public int compareTo(FeatureEntry that) {
                  return this.pack.getId().compareTo(that.pack.getId());
            }

            @Override
            protected void renderWidget(GuiGraphics gui, int pMouseX, int pMouseY, float pPartialTick) {
                  int x = getX();
                  int y = getY();

                  gui.blit(icon, x, y, 0, 0, 32, 32, 32, 32);
                  int leftPos = x + 40;
                  Font font = minecraft.font;
                  Component title = pack.getTitle();
                  gui.drawString(font, title, leftPos, y + 1, 0xFFFFFFFF);
                  gui.drawWordWrap(font, pack.getDescription(), leftPos, y + 13, getRowWidth() - 48, 0xFFFFFFFF);

                  if (isHovered()) {
                        int left = x - 8;
                        int middle = getY() + 16;
                        gui.drawString(font, enabled ? "disable" : "enable", left, middle, 0xFFFFFFFF);
                  }
            }

            @Override
            public void onClick(double pMouseX, double pMouseY) {
                  if (enabled) {
                        disabled_features.add(this);
                        enabled_features.removeAll(disabled_features);
                        enabled = false;
                  }
                  else {
                        enabled_features.add(this);
                        disabled_features.removeAll(enabled_features);
                        enabled = true;
                  }

                  Collections.sort(enabled_features);
                  Collections.sort(disabled_features);
            }

       }

      private ResourceLocation loadPackIcon(TextureManager pTextureManager, Pack pPack) {
            try {
                  PackResources packresources = pPack.open();

                  ResourceLocation var17;
                  label69: {
                        ResourceLocation resourcelocation1;
                        try {
                              IoSupplier<InputStream> iosupplier = packresources.getRootResource("pack.png");
                              if (iosupplier == null) {
                                    var17 = DEFAULT_ICON;
                                    break label69;
                              }

                              String s = pPack.getId();
                              String var10000 = Util.sanitizeName(s, ResourceLocation::validPathChar);
                              ResourceLocation resourcelocation = ResourceLocation.withDefaultNamespace("pack/" + var10000 + "/" + Hashing.sha1().hashUnencodedChars(s) + "/icon");
                              InputStream inputstream = iosupplier.get();

                              try {
                                    NativeImage nativeimage = NativeImage.read(inputstream);
                                    pTextureManager.register(resourcelocation, new DynamicTexture(nativeimage));
                                    resourcelocation1 = resourcelocation;
                              } catch (Throwable var13) {
                                    if (inputstream != null) {
                                          try {
                                                inputstream.close();
                                          } catch (Throwable var12) {
                                                var13.addSuppressed(var12);
                                          }
                                    }

                                    throw var13;
                              }

                              if (inputstream != null) {
                                    inputstream.close();
                              }
                        } catch (Throwable var14) {
                              if (packresources != null) {
                                    try {
                                          packresources.close();
                                    } catch (Throwable var11) {
                                          var14.addSuppressed(var11);
                                    }
                              }

                              throw var14;
                        }

                        if (packresources != null) {
                              packresources.close();
                        }

                        return resourcelocation1;
                  }

                  if (packresources != null) {
                        packresources.close();
                  }

                  return var17;
            } catch (Exception var15) {
                  Exception exception = var15;
                  Constants.LOG.warn("Failed to load icon from pack {}", pPack.getId(), exception);
                  return DEFAULT_ICON;
            }
      }
}
