package net.nok0.dsrtrs;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player cheat toggles. The server is what actually enforces them, but the client also keeps its
 * own copy so block-breaking prediction matches (otherwise the block would reappear for a moment).
 * In singleplayer both sides share this class, which is fine because set() is idempotent.
 */
public final class Features {
	public enum Feature {
		INSTABREAK,
		ANTI_HUNGER;

		public static Feature byId(int id) {
			Feature[] all = values();
			return id >= 0 && id < all.length ? all[id] : null;
		}
	}

	private static final Set<UUID> INSTABREAK = ConcurrentHashMap.newKeySet();
	private static final Set<UUID> ANTI_HUNGER = ConcurrentHashMap.newKeySet();

	private Features() {
	}

	private static Set<UUID> setFor(Feature feature) {
		return feature == Feature.INSTABREAK ? INSTABREAK : ANTI_HUNGER;
	}

	public static void set(Feature feature, UUID player, boolean enabled) {
		if (enabled) {
			setFor(feature).add(player);
		} else {
			setFor(feature).remove(player);
		}
	}

	public static boolean has(Feature feature, UUID player) {
		return setFor(feature).contains(player);
	}
}
