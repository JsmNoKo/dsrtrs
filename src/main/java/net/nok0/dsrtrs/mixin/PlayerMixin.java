package net.nok0.dsrtrs.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import net.nok0.dsrtrs.Features;

/** Instabreak: make the player's mining speed so high every breakable block finishes in one tick. */
@Mixin(Player.class)
public abstract class PlayerMixin {
	@Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
	private void dsrtrs$instabreak(BlockState state, CallbackInfoReturnable<Float> cir) {
		Player self = (Player) (Object) this;
		if (Features.has(Features.Feature.INSTABREAK, self.getUUID())) {
			cir.setReturnValue(1_000_000f);
		}
	}
}
