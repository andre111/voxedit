package me.andre111.voxedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import me.andre111.voxedit.ClientState;
import me.andre111.voxedit.Util;
import net.minecraft.client.render.WorldRenderer;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
	@ModifyVariable(method = "setupTerrain", argsOnly = true, at = @At("HEAD"), ordinal = 1)
	private boolean forceSpectatorView(boolean old) {
		if(Util.shouldUseCustomControlls(ClientState.player)) {
			return true;
		} else {
			return old;
		}
	}
}
