package net.nok0.dsrtrs;

import java.util.function.Function;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ModItems {
	public static final Item RUBY_SHARD = register("ruby_shard", Item::new, new Item.Properties());
	public static final Item RUBY = register("ruby", Item::new, new Item.Properties());
	public static final Item RUBY_ROD = register("ruby_rod", Item::new, new Item.Properties());

	private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Dsrtrs.id(name));
		return Items.registerItem(key, factory, properties);
	}

	public static void initialize() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
			entries.accept(RUBY_SHARD);
			entries.accept(RUBY);
			entries.accept(RUBY_ROD);
		});
	}
}
