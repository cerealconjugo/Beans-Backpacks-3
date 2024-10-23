package com.beansgalaxy.backpacks.data;

import com.beansgalaxy.backpacks.client.CommonAtClient;
import com.beansgalaxy.backpacks.components.reference.ReferenceFields;
import com.beansgalaxy.backpacks.components.reference.ReferenceTraitRegistry;
import com.beansgalaxy.backpacks.network.clientbound.SendEnderEntry;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class EnderStorage {
      private final HashMap<UUID, Entry> storage = new HashMap<>();

      public static EnderStorage get(Level level) {
            MinecraftServer server = level.getServer();
            if (server == null) {
                  return CommonAtClient.getEnderStorage();
            }
            else return ServerSave.getSave(server, true).enderStorage;
      }

      public Entry get(UUID uuid) {
            return storage.computeIfAbsent(uuid, key -> new Entry());
      }

      public GenericTraits get(UUID uuid, ResourceLocation location) {
            return get(uuid).get(location);
      }

      public GenericTraits get(Player player, ResourceLocation location) {
            Entry entry = get(player.getUUID());
            entry.displayName = player.getName();
            return entry.get(location);
      }

      public void set(UUID uuid, ResourceLocation location, GenericTraits traits) {
            get(uuid).traits.put(location, traits);
      }

      public void set(UUID uuid, ResourceLocation location, GenericTraits traits, Component name) {
            Entry entry = get(uuid);
            entry.traits.put(location, traits);
            entry.displayName = name;
      }
      
      public void save(CompoundTag tag) {
            storage.forEach((uuid, entry) -> {
                  DataResult<Tag> encodedStart = Entry.ENTRY_CODEC.encodeStart(NbtOps.INSTANCE, entry);
                  encodedStart.ifSuccess(entryTag ->
                              tag.put(uuid.toString(), entryTag)
                  );
            });
      }
      
      public void load(CompoundTag tag) {
            for (String key : tag.getAllKeys()) {
                  UUID uuid = UUID.fromString(key);
                  CompoundTag entryTag = tag.getCompound(key);
                  Entry.ENTRY_CODEC.decode(NbtOps.INSTANCE, entryTag).ifSuccess(pair ->
                              storage.put(uuid, pair.getFirst())
                  );
            }
      }

      public HashSet<ServerPlayer> getListeners(UUID uuid) {
            return get(uuid).listeners;
      }

      public Component getDisplayName(UUID uuid) {
            return get(uuid).displayName;
      }

      public static void sendEntry(ServerPlayer serverPlayer, UUID uuid, ResourceLocation location) {
            Entry entry = EnderStorage.get(serverPlayer.level()).get(uuid);
            SendEnderEntry.send(serverPlayer, uuid, location, entry.get(location), entry.displayName);
      }

      private static final class Entry {
            private final HashMap<ResourceLocation, GenericTraits> traits = new HashMap<>();
            private final HashSet<ServerPlayer> listeners = new HashSet<>();
            private Component displayName = Component.empty();

            private GenericTraits get(ResourceLocation location) {
                  return traits.computeIfAbsent(location, key -> {
                        ReferenceFields reference = ReferenceTraitRegistry.get(key);
                        if (reference == null)
                              return null;
                        
                        return reference.fields().asBlankTrait();
                  });
            }

            private static final Codec<Entry> ENTRY_CODEC = RecordCodecBuilder.create(in ->
                        in.group(
                                    ReferenceTrait.CODEC.listOf().fieldOf("trait").forGetter(entry -> entry.traits.entrySet().stream().map(set ->
                                                new ReferenceTrait(set.getKey(), set.getValue(), null, null)).toList()),
                                    ComponentSerialization.CODEC.fieldOf("name").forGetter(entry -> entry.displayName)
                        ).apply(in, (trait, name) -> {
                              Entry entry = new Entry();
                              entry.displayName = name;

                              trait.forEach(reference -> {
                                    ResourceLocation location = reference.location();
                                    reference.getTrait().ifPresent(genericTrait ->
                                                entry.traits.put(location, genericTrait)
                                    );
                              });

                              return entry;
                        })
            );
      }
}
