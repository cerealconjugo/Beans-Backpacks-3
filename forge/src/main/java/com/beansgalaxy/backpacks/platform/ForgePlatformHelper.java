package com.beansgalaxy.backpacks.platform;

import com.beansgalaxy.backpacks.network.Network2C;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.network.clientbound.Packet2C;
import com.beansgalaxy.backpacks.network.serverbound.Packet2S;
import com.beansgalaxy.backpacks.platform.services.IPlatformHelper;
import com.beansgalaxy.backpacks.registry.ModItems;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public void register(ModItems value) {

    }

    @Override
    public void send(Network2C network, Packet2C packet2C, ServerPlayer to) {

    }

    @Override
    public void send(Network2C network, Packet2C packet2C, MinecraftServer server) {

    }

    @Override
    public void send(Network2S network, Packet2S packet2S) {

    }

    @Override
    public <T extends GenericTraits> TraitComponentKind<T> registerBucket() {
        return null;
    }

    @Override
    public <T extends GenericTraits> TraitComponentKind<T> registerBattery() {
        return null;
    }
}