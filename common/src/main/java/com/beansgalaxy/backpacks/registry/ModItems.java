package com.beansgalaxy.backpacks.registry;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.items.EmptyEnderItem;
import com.beansgalaxy.backpacks.items.EnderItem;
import com.beansgalaxy.backpacks.platform.Services;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;

import java.util.function.UnaryOperator;

public enum ModItems {
      LEATHER_BACKPACK("backpack", "leather_backpack"),
      IRON_BACKPACK("iron_backpack", "iron_backpack"),
      GOLD_BACKPACK("gold_backpack", "gold_backpack"),
      NETHERITE_BACKPACK("netherite_backpack", "netherite_backpack",
                  properties -> properties.fireResistant().stacksTo(1)),
      ENDER_POUCH("ender_pouch", new EnderItem(), false),
      EMPTY_ENDER_POUCH("empty_ender_pouch", new EmptyEnderItem("vanilla_bundle")),
      BUNDLE("bundle", "vanilla_bundle"),
      LUNCH_BOX("lunch_box", "lunch_box"),
      COPPER_LEGGINGS("copper_leggings", "copper_leggings"),
      EXPERIENCE_VIAL("experience_vial", "experience_vial", p -> p.rarity(Rarity.UNCOMMON).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true).stacksTo(16)),
      QUIVER("quiver", "quiver"),
      NETHERITE_BUCKET("netherite_bucket", "netherite_bucket"),
      ALCHEMIST_BAG("alchemy_bag", "alchemy_bag"),
      ROSE_BOOTS("rose_boots", "rose_boots"),
      NETHERITE_LUNCH_BOX("netherite_lunch_box", "netherite_lunch_box"),
      ;

      public static final CreativeModeTab.DisplayItemsGenerator CREATIVE_TAB_GENERATOR = (params, output) -> {
            for (ModItems value : ModItems.values()) {
                  if (value.creativeIncluded)
                        output.accept(value.item);
            }
            output.accept(Items.LEATHER_LEGGINGS);
            output.accept(Items.DECORATED_POT);
      };
      public final String id;
      public final Item item;
      public final boolean creativeIncluded;

      ModItems(String id, String reference) {
            this(id, reference, p -> p.stacksTo(1));
      }

      ModItems(String id, String reference, UnaryOperator<Item.Properties> properties) {
            this.id = id;
            this.item = new Item(properties.apply(new Item.Properties().component(Traits.REFERENCE, new ReferenceTrait(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, reference)))));
            this.creativeIncluded = true;
      }

      ModItems(String id, Item item, boolean creativeIncluded) {
            this.id = id;
            this.item = item;
            this.creativeIncluded = creativeIncluded;
      }

      ModItems(String id, Item item) {
            this(id, item, true);
      }

      public static void register() {
            for (ModItems value : ModItems.values()) {
                  Services.PLATFORM.register(value);
            }
      }

      public Item get() {
            return item;
      }
}
