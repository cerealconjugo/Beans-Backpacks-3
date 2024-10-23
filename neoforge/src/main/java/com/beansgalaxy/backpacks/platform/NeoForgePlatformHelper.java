package com.beansgalaxy.backpacks.platform;

import com.beansgalaxy.backpacks.network.Network2C;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.network.clientbound.Packet2C;
import com.beansgalaxy.backpacks.network.serverbound.Packet2S;
import com.beansgalaxy.backpacks.platform.services.IPlatformHelper;
import com.beansgalaxy.backpacks.registry.ModItems;
import com.beansgalaxy.backpacks.traits.IDeclaredFields;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.bundle.BundleTraits;
import com.beansgalaxy.backpacks.traits.generic.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

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

    @Override
    public SoundEvent soundEvent(String id) {
        return null;
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
    public void openBundleMenu(Player player, BackpackEntity backpack, BundleTraits.Mutable mutable) {

    }

    @Override
    public <T extends GenericTraits> TraitComponentKind<T, ? extends IDeclaredFields> registerBucket() {
        return null;
    }

    @Override
    public <T extends GenericTraits> TraitComponentKind<T, ? extends IDeclaredFields> registerBattery() {
        return null;
    }
}