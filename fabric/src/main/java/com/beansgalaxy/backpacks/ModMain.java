package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.events.NetworkPackages;
import com.beansgalaxy.backpacks.events.SyncDataEvent;
import com.beansgalaxy.backpacks.registry.ModItems;
import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.trait.battery.BatteryTraits;
import com.beansgalaxy.backpacks.trait.bucket.BucketTraits;
import com.beansgalaxy.backpacks.components.EnderTraits;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.bundle.BundleMenu;
import com.beansgalaxy.backpacks.traits.bundle.BundleTraits;
import com.beansgalaxy.backpacks.traits.generic.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import team.reborn.energy.api.EnergyStorage;

import java.util.HashMap;
import java.util.Optional;

public class ModMain implements ModInitializer {
    public static final HashMap<String, SoundEvent> SOUNDS = new HashMap<>();

    @Override
    public void onInitialize() {
        NetworkPackages.registerCommon();
        registerSounds();
        ModItems.register();
        Traits.register();

        EnergyStorage.ITEM.registerFallback((stack, ctx) -> {
            BatteryTraits batteryTraits = (BatteryTraits) stack.get(Traits.BATTERY);
            if (batteryTraits != null)
                return batteryTraits.energyMutable(PatchedComponentHolder.of(stack));

            ReferenceTrait reference = stack.get(Traits.REFERENCE);
            if (reference != null && !reference.isEmpty()) {
                Optional<GenericTraits> trait = reference.getTrait();
                if (trait.isPresent() && trait.get() instanceof BatteryTraits battery)
                    return battery.energyMutable(PatchedComponentHolder.of(stack));
            }

            EnderTraits enderTraits = stack.get(Traits.ENDER);
            if (enderTraits != null) {
                Optional<GenericTraits> trait = enderTraits.getTrait();
                if (trait.isPresent() && trait.get() instanceof BatteryTraits battery)
                    return battery.energyMutable(PatchedComponentHolder.of(stack));
            }
            return null;
        });

        FluidStorage.ITEM.registerFallback((stack, ctx) -> {
            BucketTraits bucketTraits = (BucketTraits) stack.get(Traits.BUCKET);
            if (bucketTraits != null)
                return bucketTraits.mutable();

            ReferenceTrait reference = stack.get(Traits.REFERENCE);
            if (reference != null && !reference.isEmpty()) {
                Optional<GenericTraits> trait = reference.getTrait();
                if (trait.isPresent() && trait.get() instanceof BucketTraits bucket)
                    return bucket.mutable();
            }

            EnderTraits enderTraits = stack.get(Traits.ENDER);
            if (enderTraits != null) {
                Optional<GenericTraits> trait = enderTraits.getTrait();
                if (trait.isPresent() && trait.get() instanceof BucketTraits bucket)
                    return bucket.mutable();
            }

            return null;
        });

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(new SyncDataEvent());
        CommonClass.init();
    }

    public static void registerSounds() {
        for (ModSound.Events value : ModSound.Events.values()) {
            String id = value.id;
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, id);
            SoundEvent event = SoundEvent.createVariableRangeEvent(location);
            SoundEvent register = Registry.register(BuiltInRegistries.SOUND_EVENT, location, event);
            SOUNDS.put(id, register);
        }
    }

    public static final CreativeModeTab BACKPACK_TAB = FabricItemGroup.builder()
                .title(Component.translatable("itemGroup." + Constants.MOD_ID))
                .icon(() -> ModItems.LEATHER_BACKPACK.get().getDefaultInstance())
                .displayItems(ModItems.CREATIVE_TAB_GENERATOR).build();

    public static final CreativeModeTab CREATIVE_TAB =
                Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                            ResourceLocation.parse(Constants.MOD_ID + ":backpacks"), BACKPACK_TAB);


    public static Item registerItem(String name, Item item) {
        ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name);
        return Registry.register(BuiltInRegistries.ITEM, resourceLocation, item);
    }

    public record BundleMenuRecord(int entityId, BundleTraits bundleTraits) { }

    public static final StreamCodec<RegistryFriendlyByteBuf, BundleMenuRecord> BUNDLE_MENU_STREAM = new StreamCodec<>() {
        @Override
        public BundleMenuRecord decode(RegistryFriendlyByteBuf buf) {
            return new BundleMenuRecord(buf.readInt(), Traits.BUNDLE.streamCodec().decode(buf));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, BundleMenuRecord record) {
            buf.writeInt(record.entityId);
            Traits.BUNDLE.streamCodec().encode(buf, record.bundleTraits);
        }
    };

    public static final MenuType<BundleMenu> BUNDLE_MENU =
                Registry.register(BuiltInRegistries.MENU,
                            ResourceLocation.parse(Constants.MOD_ID + ":bundle_menu"),
                            new ExtendedScreenHandlerType<>(((syncId, inventory, data) -> {
                                Entity entity = inventory.player.level().getEntity(data.entityId);
                                BackpackEntity bundleEntity = entity instanceof BackpackEntity backpackEntity
                                            ? backpackEntity
                                            : null;

                                return new BundleMenu(ModMain.BUNDLE_MENU, syncId, inventory, bundleEntity, data.bundleTraits.mutable());
                            }), BUNDLE_MENU_STREAM)
                );
}
