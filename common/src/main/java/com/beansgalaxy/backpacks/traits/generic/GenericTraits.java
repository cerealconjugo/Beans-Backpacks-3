package com.beansgalaxy.backpacks.traits.generic;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.IDeclaredFields;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public interface GenericTraits {

      String name();

      IDeclaredFields fields();

      IClientTraits client();

      default <T extends GenericTraits> TraitComponentKind<T, ? extends IDeclaredFields> kind() {
            return (TraitComponentKind<T, ? extends IDeclaredFields>) fields().kind();
      }

      <T extends GenericTraits> T toReference(ResourceLocation location);

      default ModSound sound() {
            return fields().sound();
      }

      int size();

      Fraction fullness();

      default boolean isFull() {
            Fraction fullness = fullness();
            int i = fullness.compareTo(Fraction.ONE);
            return i >= 0;
      }

      void stackedOnMe(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir);

      void stackedOnOther(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir);

      boolean isEmpty();

      default void useOn(UseOnContext ctx, ItemStack backpack, CallbackInfoReturnable<InteractionResult> cir) {

      }

      default void use(Level level, Player player, InteractionHand hand, ItemStack backpack, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {

      }

      default void inventoryTick(PatchedComponentHolder stack, Level level, Entity entity, int slot, boolean selected) {

      }

      MutableTraits mutable();

      default boolean isStackable() {
            return false;
      }

      interface MutableTraits {

            GenericTraits freeze();

            @Nullable
            ItemStack addItem(ItemStack stack, Player player);

            ItemStack removeItemNoUpdate(ItemStack carried, Player player);

            void dropItems(Entity backpackEntity);

            InteractionResult interact(BackpackEntity backpackEntity, Player player, InteractionHand hand);

            GenericTraits trait();

            default ModSound sound() {
                  return trait().sound();
            }

            default void damageTrait(BackpackEntity backpackEntity, int damage, boolean silent) {

            }

            default void entityTick(BackpackEntity backpackEntity) {

            }

            default void onPlace(BackpackEntity backpackEntity, Player player, ItemStack backpackStack) {

            }

            default void onPickup(BackpackEntity backpackEntity, Player player) {

            }
      }
}
