package com.beansgalaxy.backpacks.util;

import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record TraitTooltip<T extends GenericTraits>(T traits, ItemStack itemstack,
                                                    net.minecraft.network.chat.Component title) implements TooltipComponent {
}
