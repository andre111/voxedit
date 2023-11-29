package me.andre111.voxedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import me.andre111.voxedit.ClientState;
import me.andre111.voxedit.Util;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {
	public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
		super(world, profile);
	}
	
	@Shadow
    public Input input;

	@Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
	private void onPushOutOfBlocks(double x, double z, CallbackInfo ci) {
		if(Util.shouldUseCustomControlls(this)) {
			ci.cancel();
		}
	}
	
	@Inject(method = "tickNewAi", at = @At("TAIL"))
    public void onTickNewAi(CallbackInfo ci) {
		if(Util.shouldUseCustomControlls(this)) {
			float movementVertical = 0;
			if(input.jumping) movementVertical += 1;
			if(input.sneaking) movementVertical -= 1;
			Vec3d movementInput = new Vec3d(input.movementSideways, movementVertical, input.movementForward);
			
			double inputStrength = movementInput.lengthSquared();
	        if (inputStrength < 1.0E-7) {
	            setVelocity(Vec3d.ZERO);
	        } else {
		        Vec3d movementSpeed = (inputStrength > 1.0 ? movementInput.normalize() : movementInput).multiply(ClientState.cameraSpeed);
		        float sin = MathHelper.sin(getYaw() * ((float)Math.PI / 180));
		        float cos = MathHelper.cos(getYaw() * ((float)Math.PI / 180));
		        setVelocity(new Vec3d(movementSpeed.x * cos - movementSpeed.z * sin, movementSpeed.y, movementSpeed.z * cos + movementSpeed.x * sin));
	        }
	        
	        forwardSpeed = 0;
	        sidewaysSpeed = 0;
		}
    }
}
