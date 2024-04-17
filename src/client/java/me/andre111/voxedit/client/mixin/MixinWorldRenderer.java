/*
 * Copyright (c) 2024 André Schweiger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.andre111.voxedit.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import me.andre111.voxedit.VoxEditUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
	@ModifyVariable(method = "setupTerrain", argsOnly = true, at = @At("HEAD"), ordinal = 1)
	private boolean forceSpectatorView(boolean old) {
		if(VoxEditUtil.shouldUseCustomControls(MinecraftClient.getInstance().player)) {
			return true;
		} else {
			return old;
		}
	}
}
