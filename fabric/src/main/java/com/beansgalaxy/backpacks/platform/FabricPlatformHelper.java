package com.beansgalaxy.backpacks.platform;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.network.Network2C;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.network.clientbound.Packet2C;
import com.beansgalaxy.backpacks.network.serverbound.Packet2S;
import com.beansgalaxy.backpacks.registry.ModItems;
import com.beansgalaxy.backpacks.platform.services.IPlatformHelper;
import com.beansgalaxy.backpacks.trait.battery.BatteryCodecs;
import com.beansgalaxy.backpacks.trait.battery.BatteryTraits;
import com.beansgalaxy.backpacks.trait.bucket.BucketCodecs;
import com.beansgalaxy.backpacks.trait.bucket.BucketTraits;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

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
    public void register(ModItems item) {
        ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, item.id);
        Registry.register(BuiltInRegistries.ITEM, resourceLocation, item.item);
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
}
