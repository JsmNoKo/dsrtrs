package net.nok0.dsrtrs.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.nok0.dsrtrs.Dsrtrs;

/** Sent from the client to the server when the "kill nearby mobs" key (R) is pressed. */
public record KillMobsPayload() implements CustomPacketPayload {
	public static final KillMobsPayload INSTANCE = new KillMobsPayload();

	public static final CustomPacketPayload.Type<KillMobsPayload> TYPE =
			new CustomPacketPayload.Type<>(Dsrtrs.id("kill_mobs"));

	public static final StreamCodec<RegistryFriendlyByteBuf, KillMobsPayload> CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
