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

import me.andre111.voxedit.client.ClientState;
import me.andre111.voxedit.client.gui.screen.ToolSettingsScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

@Environment(value=EnvType.CLIENT)
public class HudRenderer implements HudRenderCallback {
	private static ToolSettingsScreen TOOL_SETTINGS;
	
	public static void init() {
		HudRenderCallback.EVENT.register(new HudRenderer());
	}
	
	public static ToolSettingsScreen getToolSettingsScreen() {
		if(TOOL_SETTINGS == null) TOOL_SETTINGS = new ToolSettingsScreen();
		return TOOL_SETTINGS;
	}

	@Override
	public void onHudRender(DrawContext drawContext, float tickDelta) {
		var currentScreen = MinecraftClient.getInstance().currentScreen;
		if(currentScreen != null) {
			if(currentScreen == getToolSettingsScreen()) {
				int width = MinecraftClient.getInstance().getWindow().getScaledWidth();
				int height =  MinecraftClient.getInstance().getWindow().getScaledHeight();
				drawContext.fillGradient(0, 0, width, height, 0xA0101010, 0xB0101010);
				drawContext.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, "<- Edit Tool Settings", width/2, height/2, 0xFFFFFFFF);
			}
			return;
		}
		
		if(ClientState.tool() == null) return;
		
		var screen = getToolSettingsScreen();
		screen.render(drawContext, -1, -1, tickDelta);
	}
}
