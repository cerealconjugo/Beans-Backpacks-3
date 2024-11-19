package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.platform.Services;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.util.ModItems;
import com.beansgalaxy.backpacks.util.ModSound;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class CommonClass {
    public static final Supplier<EntityType<BackpackEntity>> BACKPACK_ENTITY =
                Services.PLATFORM.registerEntity("backpack",
                            EntityType.Builder.of(BackpackEntity::new, MobCategory.MISC)
                                        .sized(7/16f, 9/16f)
                                        .eyeHeight(0.5f)
                );

    public static final String MOD_ID = "beansbackpacks";
    public static final String MOD_NAME = "Beans' Backpacks";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
    public static final int DEFAULT_LEATHER_COLOR = 0xFF8A4821;

    public static void init() {
        ModSound.Events.register();
        Traits.register();
        ITraitData.register();
        ModItems.register();
    }

    public static boolean isEmpty(String string) {
            return string == null || string.isEmpty() || string.isBlank();
    }

    public static boolean isEmpty(Component component) {
            return component == null || component.getContents().toString().equals("empty");
    }

    public static Component getName(ItemStack stack) {
            MutableComponent name = Component.empty().append(stack.getHoverName());
            if (stack.has(DataComponents.CUSTOM_NAME)) {
                name.withStyle(ChatFormatting.ITALIC);
            }

            if (!stack.isEmpty()) {
                name.withStyle(stack.getRarity().color())
                            .withStyle($$0x -> $$0x.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(stack))));
            }

            return name;
    }
}