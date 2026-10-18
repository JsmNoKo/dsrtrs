package net.nok0.dsrtrs;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.phys.AABB;

/** Server-side logic: kill every mob from the {@link MobList} within {@link #RADIUS} blocks of a player. */
public final class MobPurge {
	public static final double RADIUS = 10.0;

	private MobPurge() {
	}

	public static void purge(ServerPlayer player) {
		ServerLevel level = player.level();
		AABB box = player.getBoundingBox().inflate(RADIUS);
		double maxDistSq = RADIUS * RADIUS;

		List<Mob> targets = level.getEntitiesOfClass(Mob.class, box,
				mob -> mob.isAlive()
						&& mob.distanceToSqr(player) <= maxDistSq
						&& MobList.contains(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString())
						&& !isProtected(mob));

		for (Mob mob : targets) {
			mob.kill(level);
		}

		player.sendSystemMessage(Component.literal("Killed " + targets.size() + " mobs"), true);
	}

	/** Things you almost certainly don't want to lose by accident in your base. */
	private static boolean isProtected(Entity mob) {
		return mob instanceof Npc // villagers, wandering traders
				|| (mob instanceof TamableAnimal pet && pet.isTame()) // your dogs/cats/parrots
				|| mob.hasCustomName(); // name-tagged mobs
	}
}
