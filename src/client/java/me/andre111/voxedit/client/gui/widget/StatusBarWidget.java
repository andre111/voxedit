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
package me.andre111.voxedit.client.gui.widget;

import me.andre111.voxedit.client.gui.Textures;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class StatusBarWidget extends ClickableWidget {
	private Text status;
	
	public StatusBarWidget(int x, int y, int width, int height) {
		super(x, y, width, height, Text.empty());
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		// background
		context.drawTexture(Textures.BACKGROUND, getX(), getY(), 0, 0, getWidth(), getHeight());
        context.drawTexture(Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE, getX(), getY()-2, 0.0f, 0.0f, getWidth(), 2, 32, 2);
		
        if(status != null) {
        	TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        	int textWidth = textRenderer.getWidth(status);
        	context.drawText(textRenderer, status, getRight() - textWidth - 8, getY() + 6, 0xFFFFFFFF, true);
        }
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
	}
	
	public void setStatus(Text status) {
		this.status = status;
	}
}
