package net.nok0.dsrtrs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;

/**
 * Finds the nearest chest / trapped chest / barrel / shulker box (through walls — it reads loaded
 * block data, it doesn't need line of sight) and tells you where it is on the action bar.
 */
public final class StashFinder {
	private static final int RADIUS = 32;
	private static final String[] SECTORS = {
			"ahead", "ahead-right", "right", "behind-right", "behind", "behind-left", "left", "ahead-left"
	};

	private StashFinder() {
	}

	private static boolean isContainer(Block block) {
		return block instanceof ChestBlock || block instanceof BarrelBlock || block instanceof ShulkerBoxBlock;
	}

	public static void update(Minecraft client) {
		LocalPlayer player = client.player;
		ClientLevel level = client.level;
		if (player == null || level == null) {
			return;
		}

		BlockPos origin = player.blockPosition();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		BlockPos best = null;
		double bestDistSq = Double.MAX_VALUE;

		for (int dx = -RADIUS; dx <= RADIUS; dx++) {
			for (int dy = -RADIUS; dy <= RADIUS; dy++) {
				for (int dz = -RADIUS; dz <= RADIUS; dz++) {
					double distSq = (double) dx * dx + (double) dy * dy + (double) dz * dz;
					if (distSq >= bestDistSq || distSq > (double) RADIUS * RADIUS) {
						continue;
					}
					cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
					if (isContainer(level.getBlockState(cursor).getBlock())) {
						best = cursor.immutable();
						bestDistSq = distSq;
					}
				}
			}
		}

		if (best == null) {
			client.gui.setOverlayMessage(Component.literal("Stash finder: nothing within " + RADIUS + " blocks"), false);
			return;
		}

		double dx = best.getX() + 0.5 - player.getX();
		double dz = best.getZ() + 0.5 - player.getZ();
		double dy = best.getY() - player.getY();
		// Minecraft yaw: 0 = south (+z), increasing clockwise. Positive diff = target is to your right.
		float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
		float diff = Mth.wrapDegrees(targetYaw - player.getYRot());
		int sector = ((Math.round(diff / 45f) % 8) + 8) % 8;

		String vertical = Math.abs(dy) < 2 ? "same level" : (dy > 0 ? Math.round(dy) + " up" : Math.round(-dy) + " down");
		client.gui.setOverlayMessage(Component.literal(String.format(
				"Stash: %d blocks %s, %s  (%d, %d, %d)",
				Math.round(Math.sqrt(bestDistSq)), SECTORS[sector], vertical,
				best.getX(), best.getY(), best.getZ())), false);
	}
}
