/*
 * Copyright (c) 2023 André Schweiger
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
package me.andre111.voxedit.client.renderer;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;

import me.andre111.voxedit.client.VoxEditClient;
import me.andre111.voxedit.client.gui.screen.EditorScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

@Environment(value=EnvType.CLIENT)
public class HudRenderer implements HudRenderCallback {
	public static void init() {
		HudRenderCallback.EVENT.register(new HudRenderer());
	}

	@Override
	public void onHudRender(DrawContext drawContext, float tickDelta) {
		var currentScreen = MinecraftClient.getInstance().currentScreen;
		if(currentScreen != null) return;
		
		if(EditorScreen.get().isActive()) {
			MinecraftClient.getInstance().getWindow().setScaleFactor(1);
			Matrix4f projMat = RenderSystem.getProjectionMatrix();
			RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0.0f, (float) MinecraftClient.getInstance().getWindow().getFramebufferWidth(), (float) MinecraftClient.getInstance().getWindow().getFramebufferHeight(), 0.0f, 1000.0f, 21000.0f), VertexSorter.BY_Z);
			EditorScreen.get().render(drawContext, -1, -1, tickDelta);
			RenderSystem.setProjectionMatrix(projMat, VertexSorter.BY_Z);
			VoxEditClient.restoreGuiScale();
		}
	}
}
