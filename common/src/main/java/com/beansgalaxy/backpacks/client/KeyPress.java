package com.beansgalaxy.backpacks.client;

import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.network.serverbound.BackpackUse;
import com.beansgalaxy.backpacks.network.serverbound.BackpackUseOn;
import com.beansgalaxy.backpacks.network.serverbound.SyncHotkey;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.chest.screen.MenuChestScreen;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicReference;

public class KeyPress {
      public static final KeyPress INSTANCE = new KeyPress();

      public static final String KEY_CATEGORY = "key.beansbackpacks.category";
      public static final String ACTION_KEY_IDENTIFIER = "key.beansbackpacks.action";
      public static final String MENUS_KEY_IDENTIFIER = "key.beansbackpacks.inventory";
      public static final String ACTION_KEY_DESC = "key.beansbackpacks.desc.action";
      public static final String MENUS_KEY_DESC = "key.beansbackpacks.desc.inventory";
      public static final String SHORTHAND_KEY_IDENTIFIER = "key.beansbackpacks.shorthand";
      public static final String UTILITY_KEY_IDENTIFIER = "key.beansbackpacks.utility";

      public final KeyMapping ACTION_KEY = new KeyMapping(
                  ACTION_KEY_IDENTIFIER,
                  GLFW.GLFW_KEY_UNKNOWN,
                  KEY_CATEGORY);

      public final KeyMapping MENUS_KEY = new KeyMapping(
                  MENUS_KEY_IDENTIFIER,
                  GLFW.GLFW_KEY_UNKNOWN,
                  KEY_CATEGORY);

      public final KeyMapping SHORTHAND_KEY = new KeyMapping(
                  SHORTHAND_KEY_IDENTIFIER,
                  GLFW.GLFW_KEY_GRAVE_ACCENT,
                  KEY_CATEGORY);

      public final KeyMapping UTILITY_KEY = new KeyMapping(
                  UTILITY_KEY_IDENTIFIER,
                  GLFW.GLFW_KEY_0,
                  KEY_CATEGORY);

      public void tick(Minecraft minecraft, LocalPlayer player) {
            isPressed actionKey = KeyPress.isPressed(minecraft, KeyPress.getActionKeyBind());
            isPressed menusKey = KeyPress.isPressed(minecraft, KeyPress.getMenusKeyBind());
            int tinyChestSlot = minecraft.screen instanceof MenuChestScreen screen ? screen.slotIndex() : -1;
            boolean menuKeyPressed = tinyChestSlot == -1 && menusKey.pressed();

//            if(actionKey.pressed() && minecraft.hitResult instanceof BlockHitResult hitResult && Constants.CLIENT_CONFIG.instant_place.get())
//                  consumeActionUseOn(minecraft, hitResult);

            BackData backData = BackData.get(player);

            if (actionKey.pressed() == backData.isActionKeyDown() && menuKeyPressed == backData.isMenuKeyDown() && tinyChestSlot == backData.getTinySlot())
                  return;

            backData.setActionKey(actionKey.pressed());
            backData.setMenuKey(menuKeyPressed);
            backData.setTinySlot(tinyChestSlot);
            SyncHotkey.send(actionKey.pressed(), menuKeyPressed, tinyChestSlot);

//            boolean instantPlace = Constants.CLIENT_CONFIG.instant_place.get();
//            if (actionKey.pressed() && (instantPlace || actionKey.onMouse()) && minecraft.screen == null) {
//                  KeyPress.instantPlace(instance);
//            }
//            else if (menusKey.pressed() && menusKey.onMouse() && minecraft.screen instanceof ClickAccessor clickAccessor)
//                  clickAccessor.beans_Backpacks_2$instantPlace();

      }

      public boolean consumeActionUse(Level level, Player player) {
            ItemStack backStack = player.getItemBySlot(EquipmentSlot.BODY);
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir =
                        new CallbackInfoReturnable<>("backpack_action_use", true, InteractionResultHolder.pass(backStack));

            Traits.runIfPresent(backStack, traits -> {
                  traits.use(level, player, InteractionHand.MAIN_HAND, PatchedComponentHolder.of(backStack), cir);
            });

            if (cir.getReturnValue().getResult().consumesAction()) {
                  BackpackUse.send();
                  return true;
            }

            return false;
      }

      public boolean consumeActionUseOn(Minecraft instance, BlockHitResult hitResult) {
            if (!instance.level.getWorldBorder().isWithinBounds(hitResult.getBlockPos()))
                  return false;

            LocalPlayer player = instance.player;
            boolean cancel = player.isSprinting() || player.isSwimming();
            if (cancel && INSTANCE.ACTION_KEY.isUnbound())
                  storeCoyoteClick(instance);
            else {
                  return placeBackpack(instance.player, hitResult);
            }

            return false;
      }


      private static boolean placeBackpack(Player player, BlockHitResult hitResult) {
            AtomicReference<EquipmentSlot> equipmentSlot = new AtomicReference<>(null);

            EquipableComponent.runIfPresent(player, (equipable, slot) -> {
                  if (BackpackUseOn.placeBackpack(player, hitResult, slot))
                        equipmentSlot.set(slot);
            });

            if (equipmentSlot.get() != null) {
                  BackpackUseOn.send(hitResult, equipmentSlot.get());
                  return true;
            }

            return false;
      }

      private void storeCoyoteClick(Minecraft instance) {

      }

      public static KeyMapping getDefaultKeyBind() {
            Minecraft instance = Minecraft.getInstance();
//            if (Constants.CLIENT_CONFIG.sneak_default.get())
//                  return instance.options.keyShift;

            return instance.options.keySprint;
      }

      public static KeyMapping getActionKeyBind() {
            KeyMapping sprintKey = getDefaultKeyBind();
            KeyMapping customKey = INSTANCE.ACTION_KEY;

            return customKey.isUnbound() ? sprintKey : customKey;
      }

      public static KeyMapping getMenusKeyBind() {
            KeyMapping sprintKey = getActionKeyBind();
            KeyMapping customKey = INSTANCE.MENUS_KEY;

            return customKey.isUnbound() ? sprintKey : customKey;
      }

      public static Component getKeyReadable(KeyMapping keyBind) {
            String name = "tldr." + keyBind.saveString();
            if (Language.getInstance().has(name)) {
                  return Component.translatable(name);
            }

            return keyBind.getTranslatedKeyMessage();
      }

      public static Component getReadable(boolean inMenu) {
            KeyMapping keyBind = inMenu ? getMenusKeyBind() : getActionKeyBind();
            InputConstants.Key key = InputConstants.getKey(keyBind.saveString());
            Component readable = getKeyReadable(keyBind);
            if (InputConstants.Type.MOUSE.equals(key.getType())) {
                  return Component.translatable("help.beansbackpacks.mouse_button", readable);
            }
            boolean instantPlace = false;// Constants.CLIENT_CONFIG.instant_place.get();
            if (instantPlace) {
                  return Component.translatable("help.beansbackpacks.instant_place", readable);
            }
            if (inMenu)
                  return Component.translatable("help.beansbackpacks.menu_hotkey", readable);

            return Component.translatable("help.beansbackpacks.action_hotkey", readable, getKeyReadable(Minecraft.getInstance().options.keyUse));
      }

      public static @NotNull isPressed isPressed(Minecraft minecraft, KeyMapping bind) {
            KeyMapping sneakKey = minecraft.options.keyShift;
            if (sneakKey.same(bind))
                  sneakKey.setDown(bind.isDown());

            InputConstants.Key key = InputConstants.getKey(bind.saveString());
            long window = minecraft.getWindow().getWindow();
            int value = key.getValue();

            boolean isMouseKey = key.getType().equals(InputConstants.Type.MOUSE);
            boolean isPressed = isMouseKey ? GLFW.glfwGetMouseButton(window, value) == 1 : InputConstants.isKeyDown(window, value);
            return new isPressed(isMouseKey, isPressed);
      }

      public record isPressed(boolean onMouse, boolean pressed) {
      }
}
