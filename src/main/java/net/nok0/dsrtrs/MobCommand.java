package net.nok0.dsrtrs;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/** /dsrtrs mobs list | add <id> | remove <id>  — edits the list R kills. */
public final class MobCommand {
	private MobCommand() {
	}

	/** "cow" becomes "minecraft:cow"; "*" and namespaced IDs are kept as typed. */
	private static String normalize(String input) {
		String id = input.toLowerCase();
		return id.equals("*") || id.contains(":") ? id : "minecraft:" + id;
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("dsrtrs")
				.then(Commands.literal("mobs")
						.then(Commands.literal("list").executes(ctx -> {
							ctx.getSource().sendSuccess(
									() -> Component.literal("Kill list: " + String.join(", ", MobList.get())), false);
							return 1;
						}))
						.then(Commands.literal("add")
								.then(Commands.argument("mob", StringArgumentType.word()).executes(ctx -> {
									String id = normalize(StringArgumentType.getString(ctx, "mob"));
									boolean added = MobList.add(id);
									ctx.getSource().sendSuccess(() -> Component.literal(
											added ? "Added " + id : id + " is already in the list"), false);
									return added ? 1 : 0;
								})))
						.then(Commands.literal("remove")
								.then(Commands.argument("mob", StringArgumentType.word()).executes(ctx -> {
									String id = normalize(StringArgumentType.getString(ctx, "mob"));
									boolean removed = MobList.remove(id);
									ctx.getSource().sendSuccess(() -> Component.literal(
											removed ? "Removed " + id : id + " was not in the list"), false);
									return removed ? 1 : 0;
								})))));
	}
}
