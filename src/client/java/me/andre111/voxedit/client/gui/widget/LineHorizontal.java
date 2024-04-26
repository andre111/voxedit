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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class LineHorizontal extends ClickableWidget {
	public LineHorizontal(int width, Text text) {
		super(0, 0, width, (text == null ? 2 : 8) + 2*2, text);
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		context.drawTexture(Screen.INWORLD_HEADER_SEPARATOR_TEXTURE, getX(), getY()+getHeight()/2, 0.0f, 0.0f, width, 2, 32, 2);
		Text text = getMessage();
		if(text != null) {
			TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
			int textWidth = textRenderer.getWidth(text);
			context.fill(getX()+2, getY(), getX()+2 + textWidth+4, getY() + 12, 0xFF000000);
			context.drawBorder(getX()+2, getY(), textWidth+4, 12, 0xFFFFFFFF);
			context.drawText(textRenderer, text, getX()+4, getY()+2, 0xFFFFFFFF, true);
		}
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
	}
}
