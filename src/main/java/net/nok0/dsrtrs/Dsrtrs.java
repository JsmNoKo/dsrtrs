package net.nok0.dsrtrs;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;

import net.nok0.dsrtrs.network.FeatureTogglePayload;
import net.nok0.dsrtrs.network.KillMobsPayload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dsrtrs implements ModInitializer {
	public static final String MOD_ID = "dsrtrs";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.initialize();

		// R key: the client asks, the server does the killing.
		PayloadTypeRegistry.playC2S().register(KillMobsPayload.TYPE, KillMobsPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(KillMobsPayload.TYPE,
				(payload, context) -> MobPurge.purge(context.player()));

		// Instabreak / anti-hunger toggles from the client.
		PayloadTypeRegistry.playC2S().register(FeatureTogglePayload.TYPE, FeatureTogglePayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(FeatureTogglePayload.TYPE, (payload, context) -> {
			Features.Feature feature = Features.Feature.byId(payload.feature());
			if (feature != null) {
				Features.set(feature, context.player().getUUID(), payload.enabled());
			}
		});

		// Anti-hunger: keep food and saturation full for players who switched it on.
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (Features.has(Features.Feature.ANTI_HUNGER, player.getUUID())) {
					FoodData food = player.getFoodData();
					food.setFoodLevel(20);
					food.setSaturation(20f);
				}
			}
		});

		CommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess, environment) -> MobCommand.register(dispatcher));

		LOGGER.info("Hello Fabric world!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
