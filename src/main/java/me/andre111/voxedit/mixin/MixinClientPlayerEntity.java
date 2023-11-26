package me.andre111.voxedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.ClientPlayerEntity;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity {
	@Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
	private void onPushOutOfBlocks(double x, double z, CallbackInfo ci) {
		ci.cancel();
	}
}
