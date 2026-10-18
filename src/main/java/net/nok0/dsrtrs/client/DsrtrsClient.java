package net.nok0.dsrtrs.client;

import java.util.UUID;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;

import net.nok0.dsrtrs.Dsrtrs;
import net.nok0.dsrtrs.Features;
import net.nok0.dsrtrs.TravelMode;
import net.nok0.dsrtrs.network.FeatureTogglePayload;
import net.nok0.dsrtrs.network.KillMobsPayload;

public class DsrtrsClient implements ClientModInitializer {
	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Dsrtrs.id("main"));

	/** Vanilla creative flying speed is 0.05. This is 6x that — raise or lower to taste. */
	private static final float VANILLA_FLY_SPEED = TravelMode.VANILLA_FLY_SPEED;
	private static final float TRAVEL_FLY_SPEED = TravelMode.TRAVEL_FLY_SPEED;

	private static KeyMapping killMobsKey;
	private static KeyMapping travelKey;
	private static KeyMapping instabreakKey;
	private static KeyMapping antiHungerKey;
	private static KeyMapping stashFinderKey;

	private static boolean travelMode = false;
	private static boolean instabreak = false;
	private static boolean antiHunger = false;

	/** The player instance we last pushed our toggle state to the server for (join / respawn). */
	private static LocalPlayer syncedFor = null;

	private static KeyMapping key(String name, int keyCode) {
		return KeyMappingHelper.registerKeyMapping(new KeyMapping(name, keyCode, CATEGORY));
	}

	@Override
	public void onInitializeClient() {
		killMobsKey = key("key.dsrtrs.kill_mobs", InputConstants.KEY_R);
		travelKey = key("key.dsrtrs.travel_mode", InputConstants.KEY_K);
		instabreakKey = key("key.dsrtrs.instabreak", InputConstants.KEY_B);
		antiHungerKey = key("key.dsrtrs.anti_hunger", InputConstants.KEY_H);
		stashFinderKey = key("key.dsrtrs.stash_finder", InputConstants.KEY_G);

		ClientTickEvents.END_CLIENT_TICK.register(DsrtrsClient::tick);
	}

	private static void tick(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null) {
			syncedFor = null;
			return;
		}

		// New player instance (joined a world / respawned): make the server match our toggles.
		if (syncedFor != player) {
			syncedFor = player;
			pushFeature(player, Features.Feature.INSTABREAK, instabreak);
			pushFeature(player, Features.Feature.ANTI_HUNGER, antiHunger);
			pushFeature(player, Features.Feature.TRAVEL_MODE, travelMode);
		}

		while (killMobsKey.consumeClick()) {
			if (!ClientPlayNetworking.canSend(KillMobsPayload.TYPE)) {
				player.sendSystemMessage(Component.literal("This server doesn't have the dsrtrs mod installed"));
				continue;
			}
			ClientPlayNetworking.send(KillMobsPayload.INSTANCE);
		}

		while (travelKey.consumeClick()) {
			travelMode = !travelMode;
			pushFeature(player, Features.Feature.TRAVEL_MODE, travelMode);
			status(player, "Travel mode", travelMode);
			if (!travelMode) {
				restoreAbilities(client, player);
			}
		}

		while (instabreakKey.consumeClick()) {
			instabreak = !instabreak;
			pushFeature(player, Features.Feature.INSTABREAK, instabreak);
			status(player, "Instabreak", instabreak);
		}

		while (antiHungerKey.consumeClick()) {
			antiHunger = !antiHunger;
			pushFeature(player, Features.Feature.ANTI_HUNGER, antiHunger);
			status(player, "Anti-hunger", antiHunger);
		}

		while (stashFinderKey.consumeClick()) {
			StashFinder.update(client);
		}

		if (travelMode) {
			// Re-applied every tick so it survives respawns and dimension changes.
			player.getAbilities().mayfly = true;
			player.getAbilities().setFlyingSpeed(TRAVEL_FLY_SPEED);
		}
	}

	private static void status(LocalPlayer player, String name, boolean on) {
		player.sendSystemMessage(Component.literal(name + ": " + (on ? "ON" : "OFF")));
	}

	/** Update our local copy (used by the block-breaking prediction) and tell the server. */
	private static void pushFeature(LocalPlayer player, Features.Feature feature, boolean enabled) {
		UUID id = player.getUUID();
		Features.set(feature, id, enabled);
		ClientPlayNetworking.send(new FeatureTogglePayload(feature.ordinal(), enabled));
	}

	/** Back to whatever the current game mode normally allows. */
	private static void restoreAbilities(Minecraft client, LocalPlayer player) {
		GameType mode = client.gameMode != null ? client.gameMode.getPlayerMode() : GameType.SURVIVAL;
		boolean canFly = mode == GameType.CREATIVE || mode == GameType.SPECTATOR;
		player.getAbilities().mayfly = canFly;
		if (!canFly) {
			player.getAbilities().flying = false;
		}
		player.getAbilities().setFlyingSpeed(VANILLA_FLY_SPEED);
		player.onUpdateAbilities();
	}
}
