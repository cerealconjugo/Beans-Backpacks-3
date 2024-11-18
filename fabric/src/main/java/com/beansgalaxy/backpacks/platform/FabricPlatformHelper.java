package com.beansgalaxy.backpacks.platform;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.network.Network2C;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.network.clientbound.Packet2C;
import com.beansgalaxy.backpacks.network.serverbound.Packet2S;
import com.beansgalaxy.backpacks.platform.services.IPlatformHelper;
import com.beansgalaxy.backpacks.trait.battery.BatteryCodecs;
import com.beansgalaxy.backpacks.trait.battery.BatteryTraits;
import com.beansgalaxy.backpacks.trait.bucket.BucketCodecs;
import com.beansgalaxy.backpacks.trait.bucket.BucketTraits;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Supplier<Item> register(String id, Supplier<Item> item) {
        ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, id);
        return () -> Registry.register(BuiltInRegistries.ITEM, resourceLocation, item.get());
    }

    @Override
    public void send(Network2S network, Packet2S msg) {
        ClientPlayNetworking.send(msg);
    }

    @Override
    public void send(Network2C network, Packet2C msg, ServerPlayer to) {
        ServerPlayNetworking.send(to, msg);
    }

    @Override
    public void send(Network2C network2C, Packet2C msg, MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            send(network2C, msg, player);
        }
    }

    @Override
    public TraitComponentKind<BucketTraits> registerBucket() {
        return TraitComponentKind.register(BucketTraits.NAME, BucketCodecs.INSTANCE);
    }

    @Override
    public TraitComponentKind<BatteryTraits> registerBattery() {
        return TraitComponentKind.register(BatteryTraits.NAME, BatteryCodecs.INSTANCE);
    }

    @Override
    public <T> DataComponentType<T> registerComponents(String name, DataComponentType<T> type) {
        return Registry.register(
                    BuiltInRegistries.DATA_COMPONENT_TYPE,
                    ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), type
        );
    }

    @Override
    public <T extends Entity> Supplier<EntityType<T>> registerEntity(String name, EntityType.Builder<T> type) {
        return () -> Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), type.build(name)
        );
    }

    @Override
    public SoundEvent registerSound(String name, SoundEvent event) {
        return Registry.register(
                    BuiltInRegistries.SOUND_EVENT,
                    ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), event
        );
    }

    @Override
    public String getModelVariant() {
        return "fabric_resource";
    }

    @Override
    public ModelResourceLocation getModelVariant(ResourceLocation location) {
        return new ModelResourceLocation(location, "fabric_resource");
    }
}
