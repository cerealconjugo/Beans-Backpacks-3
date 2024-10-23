package com.beansgalaxy.backpacks.mixin;

import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Items.class)
public class ItemsMixin {
      @Redirect(method = "<clinit>", at = @At(value = "NEW", ordinal = 169,
                  target = "Lnet/minecraft/world/item/Item$Properties;"))
      private static Item.Properties addLeatherLeggingsPockets() {
            return new Item.Properties().component(Traits.REFERENCE, ReferenceTrait.of("minecraft", "leggings"));
      }

      @Redirect(method = "<clinit>", at = @At(value = "NEW", ordinal = 7,
                  target = "Lnet/minecraft/world/item/Item$Properties;"))
      private static Item.Properties addPotComponent() {
            return new Item.Properties().component(Traits.REFERENCE, ReferenceTrait.of("minecraft", "pot"));
      }
}
