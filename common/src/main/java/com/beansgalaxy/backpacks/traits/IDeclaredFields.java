package com.beansgalaxy.backpacks.traits;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class IDeclaredFields {
      private final ResourceLocation location;
      private final ModSound sound;

      public IDeclaredFields(ResourceLocation location, ModSound sound) {
            this.location = location;
            this.sound = sound;
      }

      public ModSound sound() {
            return sound;
      }

      abstract public <T extends GenericTraits> T asBlankTrait();

      public void appendLines(List<Component> lines, TooltipFlag flag) {

      }

      abstract public TraitComponentKind<? extends GenericTraits, ? extends IDeclaredFields> kind();

      public Optional<ResourceLocation> location() {
            return Optional.ofNullable(location);
      }

      public static void encodeLocation(RegistryFriendlyByteBuf buf, IDeclaredFields fields) {
            fields.location().ifPresentOrElse(location -> {
                  buf.writeBoolean(true);
                  ResourceLocation.STREAM_CODEC.encode(buf, location);
            }, () -> buf.writeBoolean(false));
      }

      @Nullable
      public static ResourceLocation decodeLocation(RegistryFriendlyByteBuf buf) {
            if (buf.readBoolean())
                  return ResourceLocation.STREAM_CODEC.decode(buf);

            return null;
      }

      abstract public <T extends IDeclaredFields> T toReference(ResourceLocation location);

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof IDeclaredFields that)) return false;
            return Objects.equals(location(), that.location())
                        && sound() == that.sound();
      }

      @Override
      public int hashCode() {
            return Objects.hash(location(), sound());
      }
}
