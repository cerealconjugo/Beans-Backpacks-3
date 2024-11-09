package com.beansgalaxy.backpacks.platform;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.network.Network2C;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.network.clientbound.Packet2C;
import com.beansgalaxy.backpacks.network.serverbound.Packet2S;
import com.beansgalaxy.backpacks.platform.services.IPlatformHelper;
import com.beansgalaxy.backpacks.registry.ModItems;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.bucket.BucketCodecs;
import com.beansgalaxy.backpacks.traits.bucket.BucketTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MOD_ID);

    @Override
    public void register(ModItems value) {
        ITEMS.register(value.id, () -> value.item);
    }

    @Override
    public void send(Network2C network, Packet2C packet2C, ServerPlayer to) {
        PacketDistributor.sendToPlayer(to, packet2C);
    }

    @Override
    public void send(Network2C network, Packet2C packet2C, MinecraftServer server) {
        PacketDistributor.sendToAllPlayers(packet2C);
    }

    @Override
    public void send(Network2S network, Packet2S packet2S) {
        PacketDistributor.sendToServer(packet2S);
    }

    @Override
    public TraitComponentKind<BucketTraits> registerBucket() {
        return TraitComponentKind.register(BucketTraits.NAME, BucketCodecs.INSTANCE);
    }

    @Override
    public <T extends GenericTraits> TraitComponentKind<T> registerBattery() {
        return null;
    }
}