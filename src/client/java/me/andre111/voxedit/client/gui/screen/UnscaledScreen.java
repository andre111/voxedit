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
package me.andre111.voxedit.client.gui.screen;

import me.andre111.voxedit.client.VoxEditClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class UnscaledScreen extends Screen {
	private final Screen parent;

	protected UnscaledScreen(Screen parent, Text title) {
		super(title);
		this.parent = parent;
		width = MinecraftClient.getInstance().getWindow().getFramebufferWidth();
		height = MinecraftClient.getInstance().getWindow().getFramebufferHeight();
	}
	
    @Override
    public void close() {
        this.client.setScreen(parent);
    }

	@Override
	public void onDisplayed() {
		VoxEditClient.unscaleGui();
		MinecraftClient.getInstance().options.hudHidden = true;
		width = MinecraftClient.getInstance().getWindow().getFramebufferWidth();
		height = MinecraftClient.getInstance().getWindow().getFramebufferHeight();
	}

	@Override
	public void removed() {
		MinecraftClient.getInstance().options.hudHidden = false;
		VoxEditClient.restoreGuiScale();
	}
}
