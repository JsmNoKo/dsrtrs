package net.nok0.dsrtrs;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;

/** Server side of travel mode: the server itself grants flight and fly speed, so it isn't rejected. */
public final class TravelMode {
	public static final float VANILLA_FLY_SPEED = 0.05f;
	public static final float TRAVEL_FLY_SPEED = VANILLA_FLY_SPEED * 6f;

	private TravelMode() {
	}

	public static void apply(ServerPlayer player, boolean on) {
		Abilities abilities = player.getAbilities();
		if (on) {
			abilities.mayfly = true;
			abilities.setFlyingSpeed(TRAVEL_FLY_SPEED);
		} else {
			boolean canFly = player.isCreative() || player.isSpectator();
			abilities.mayfly = canFly;
			if (!canFly) {
				abilities.flying = false;
			}
			abilities.setFlyingSpeed(VANILLA_FLY_SPEED);
		}
		player.onUpdateAbilities(); // tells the client
	}
}
