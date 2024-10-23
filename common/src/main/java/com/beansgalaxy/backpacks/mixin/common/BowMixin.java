package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.traits.quiver.QuiverTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;

@Mixin(BowItem.class)
public abstract class BowMixin extends ProjectileWeaponItem {
      public BowMixin(Properties pProperties) {
            super(pProperties);
      }

      @Shadow public abstract Predicate<ItemStack> getAllSupportedProjectiles();

      @Shadow public abstract int getUseDuration(ItemStack pStack, LivingEntity pEntity);


      @Inject(method = "releaseUsing", cancellable = true, at = @At(value = "INVOKE",
                  target = "Lnet/minecraft/world/entity/player/Player;getProjectile(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
      private void useBackpackQuiverArrow(ItemStack bowStack, Level level, LivingEntity pEntityLiving, int pTimeLeft, CallbackInfo ci) {
            Player player = (Player) pEntityLiving;
            Predicate<ItemStack> predicate = getAllSupportedProjectiles();
            QuiverTraits.runIfQuiverEquipped(player, (traits, slot) -> {
                  QuiverTraits.Mutable mutable = traits.mutable();
                  List<ItemStack> stacks = mutable.getItemStacks();
                  if (stacks.isEmpty())
                        return false;

                  ItemStack stack = mutable.getSelectedStackSafe(player);
                  if (predicate.test(stack)) {
                        int i = this.getUseDuration(bowStack, player) - pTimeLeft;
                        float f = BowItem.getPowerForTime(i);
                        if (!(f + 0.0 < 0.1)) {
                              List<ItemStack> list = draw(bowStack, stack, player);
                              if (level instanceof ServerLevel serverLevel) {
                                    if (!list.isEmpty()) {
                                          this.shoot(serverLevel, player, player.getUsedItemHand(), bowStack, list, f * 3.0F, 1.0F, f == 1.0F, null);
                                    }
                              }

                              level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F);
                              player.awardStat(Stats.ITEM_USED.get(this));
                              ItemStack quiver = player.getItemBySlot(slot);
                              PatchedComponentHolder holder = PatchedComponentHolder.of(quiver);
                              traits.kind().freezeAndCancel(holder, mutable);

                              int selectedSlotSafe = mutable.trait().getSelectedSlot(player);
                              traits.limitSelectedSlot(selectedSlotSafe, traits.stacks().size());
                              ci.cancel();

                              if (player instanceof ServerPlayer serverPlayer) {
                                    List<Pair<EquipmentSlot, ItemStack>> pSlots = List.of(Pair.of(slot, quiver));
                                    ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(serverPlayer.getId(), pSlots);
                                    serverPlayer.serverLevel().getChunkSource().broadcastAndSend(serverPlayer, packet);
                              }
                        }

                        return true;
                  }

                  return false;
            });
      }
}
