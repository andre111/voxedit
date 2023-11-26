package me.andre111.voxedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

@Mixin(Entity.class)
public abstract class MixinEntity {
	@Shadow
    public boolean noClip;

	@Inject(method = "move", at = @At("HEAD"))
    public void move(MovementType movementType, Vec3d movement, CallbackInfo ci) {
		if((Object) this instanceof PlayerEntity player && player.isCreative() && player.getAbilities().flying) {
			noClip = true;
		}
	}
}
