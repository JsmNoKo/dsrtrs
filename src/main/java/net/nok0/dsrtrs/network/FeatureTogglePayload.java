package net.nok0.dsrtrs.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.nok0.dsrtrs.Dsrtrs;

/** Client -> server: set a feature (see Features.Feature ordinal) on or off. Explicit state, not a toggle. */
public record FeatureTogglePayload(int feature, boolean enabled) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<FeatureTogglePayload> TYPE =
			new CustomPacketPayload.Type<>(Dsrtrs.id("feature_toggle"));

	public static final StreamCodec<RegistryFriendlyByteBuf, FeatureTogglePayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, FeatureTogglePayload::feature,
			ByteBufCodecs.BOOL, FeatureTogglePayload::enabled,
			FeatureTogglePayload::new);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
