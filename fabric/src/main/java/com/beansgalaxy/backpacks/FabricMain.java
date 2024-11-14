package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.events.NetworkPackages;
import com.beansgalaxy.backpacks.events.SyncDataEvent;
import com.beansgalaxy.backpacks.registry.ModItems;
import com.beansgalaxy.backpacks.trait.battery.BatteryTraits;
import com.beansgalaxy.backpacks.trait.bucket.BucketTraits;
import com.beansgalaxy.backpacks.components.EnderTraits;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import team.reborn.energy.api.EnergyStorage;

import java.util.Optional;

public class FabricMain implements ModInitializer {

    public static final DataComponentType<FluidVariant>
                DATA_FLUID = Traits.register("data_fluid", FluidVariant.CODEC, FluidVariant.PACKET_CODEC);

    @Override
    public void onInitialize() {
        NetworkPackages.registerCommon();
        ModItems.register();

        EnergyStorage.ITEM.registerFallback((stack, ctx) -> {
            BatteryTraits batteryTraits = (BatteryTraits) stack.get(Traits.BATTERY);
            if (batteryTraits != null)
                return batteryTraits.mutable(PatchedComponentHolder.of(stack)).getStorage();

            ReferenceTrait reference = stack.get(Traits.REFERENCE);
            if (reference != null && !reference.isEmpty()) {
                Optional<GenericTraits> trait = reference.getTrait();
                if (trait.isPresent() && trait.get() instanceof BatteryTraits battery)
                    return battery.mutable(PatchedComponentHolder.of(stack)).getStorage();
            }

            EnderTraits enderTraits = stack.get(Traits.ENDER);
            if (enderTraits != null) {
                Optional<GenericTraits> trait = enderTraits.getTrait();
                if (trait.isPresent() && trait.get() instanceof BatteryTraits battery)
                    return battery.mutable(PatchedComponentHolder.of(stack)).getStorage();
            }
            return null;
        });

        FluidStorage.ITEM.registerFallback((stack, ctx) -> {
            BucketTraits bucketTraits = (BucketTraits) stack.get(Traits.BUCKET);
            if (bucketTraits != null)
                return bucketTraits.mutable(PatchedComponentHolder.of(stack));

            ReferenceTrait reference = stack.get(Traits.REFERENCE);
            if (reference != null && !reference.isEmpty()) {
                Optional<GenericTraits> trait = reference.getTrait();
                if (trait.isPresent() && trait.get() instanceof BucketTraits bucket)
                    return bucket.mutable(PatchedComponentHolder.of(stack));
            }

            EnderTraits enderTraits = stack.get(Traits.ENDER);
            if (enderTraits != null) {
                Optional<GenericTraits> trait = enderTraits.getTrait();
                if (trait.isPresent() && trait.get() instanceof BucketTraits bucket)
                    return bucket.mutable(PatchedComponentHolder.of(stack));
            }

            return null;
        });

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(new SyncDataEvent());
        CommonClass.init();
    }

    public static final CreativeModeTab BACKPACK_TAB = FabricItemGroup.builder()
                .title(Component.translatable("itemGroup." + Constants.MOD_ID))
                .icon(() -> ModItems.LEATHER_BACKPACK.get().getDefaultInstance())
                .displayItems(ModItems.CREATIVE_TAB_GENERATOR).build();

    public static final CreativeModeTab CREATIVE_TAB =
                Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                            ResourceLocation.parse(Constants.MOD_ID + ":backpacks"), BACKPACK_TAB);


}
