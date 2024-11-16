package com.beansgalaxy.backpacks;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Constants {

	public static final String MOD_ID = "beansbackpacks";
	public static final String MOD_NAME = "Beans' Backpacks";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
	public static final int DEFAULT_LEATHER_COLOR = 0xFF8A4821;

	public static boolean isEmpty(String string) {
		return string == null || string.isEmpty() || string.isBlank();
	}

	public static boolean isEmpty(Component component) {
		return component == null || component.getContents().toString().equals("empty");
	}

	public static Component getName(ItemStack stack) {
		MutableComponent name = Component.empty().append(stack.getHoverName());
		if (stack.has(DataComponents.CUSTOM_NAME)) {
			name.withStyle(ChatFormatting.ITALIC);
		}

		if (!stack.isEmpty()) {
			name.withStyle(stack.getRarity().color())
					.withStyle($$0x -> $$0x.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(stack))));
		}

		return name;
	}

	public static <T> StreamCodec<RegistryFriendlyByteBuf, List<T>> createListStream(StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
		return new StreamCodec<>() {
			@Override
			public void encode(RegistryFriendlyByteBuf buf, List<T> list) {
				int size = list.size();
				buf.writeInt(size);
				list.forEach(entry -> streamCodec.encode(buf, entry));
			}

			@Override
			public List<T> decode(RegistryFriendlyByteBuf buf) {
				int size = buf.readInt();
				ArrayList<T> list = new ArrayList<>();
				for (int i = 0; i < size; i++)
					list.add(streamCodec.decode(buf));

				return (List<T>) List.of(list.toArray());
			}
		};
	}

	public static final ItemStack NO_GUI_STAND_IN = new ItemStack(Items.AIR);
	public static final ClampedItemPropertyFunction NO_GUI_PREDICATE = (itemStack, clientLevel, livingEntity, i) -> {
		if (itemStack == NO_GUI_STAND_IN && clientLevel == null && livingEntity == null && i == 0)
			return 1;

		return 0;
	};
}