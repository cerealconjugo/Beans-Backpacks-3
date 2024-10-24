package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.platform.Services;
import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.BackpackEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Items;

public class CommonClass {
    public static final EntityType<BackpackEntity> BACKPACK_ENTITY = Registry.register(
                BuiltInRegistries.ENTITY_TYPE, ResourceLocation.parse(Constants.MOD_ID + ":backpack"),
                EntityType.Builder.of(BackpackEntity::new, MobCategory.MISC)
                            .sized(7/16f, 9/16f).eyeHeight(0.5f).build("backpack")
    );

    public static void init() {
        Traits.register();
        ModSound.register();

        EntityDataSerializers.registerSerializer(BackpackEntity.TRAIT.serializer());
        EntityDataSerializers.registerSerializer(BackpackEntity.PLACEABLE.serializer());


        Constants.LOG.info("Hello from Common init on {}! we are currently in a {} environment!", Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());
        Constants.LOG.info("The ID for diamonds is {}", BuiltInRegistries.ITEM.getKey(Items.DIAMOND));
    }

}