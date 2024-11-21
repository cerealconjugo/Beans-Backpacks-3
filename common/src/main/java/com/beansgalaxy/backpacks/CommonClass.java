package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.platform.Services;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.util.ModItems;
import com.beansgalaxy.backpacks.util.ModSound;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.function.Supplier;

public class CommonClass {
    public static final Supplier<EntityType<BackpackEntity>> BACKPACK_ENTITY =
                Services.PLATFORM.register("backpack",
                            EntityType.Builder.of(BackpackEntity::new, MobCategory.MISC)
                                        .sized(7/16f, 9/16f)
                                        .eyeHeight(0.5f)
                );

    public static final Holder<Attribute> TOOL_BELT_ATTRIBUTE = Services.PLATFORM.register("player.tool_belt",
                new RangedAttribute("attribute.name.player.tool_belt", 2, 0, 8).setSyncable(true));
    public static final Holder<Attribute> SHORTHAND_ATTRIBUTE = Services.PLATFORM.register("player.shorthand",
                new RangedAttribute("attribute.name.player.shorthand", 1, 0, 8).setSyncable(true));

      public static void init() {
        ModSound.Events.register();
        Traits.register();
        ITraitData.register();
        ModItems.register();
    }

}