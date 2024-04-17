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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import me.andre111.voxedit.client.gui.Textures;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class MenuWidget extends ClickableWidget {
	private final List<Category> categories = new ArrayList<>();
	
	public MenuWidget(int x, int y, int width, int height) {
		super(x, y, width, height, Text.empty());
	}
	
	public Category addCategory(Text text) {
		Category category = new Category(text);
		categories.add(category);
		return category;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		// background
		context.drawTexture(Textures.BACKGROUND, getX(), getY(), 0, 0, getWidth(), getHeight());
        context.drawTexture(Screen.INWORLD_HEADER_SEPARATOR_TEXTURE, getX(), getY()+getHeight(), 0.0f, 0.0f, getWidth(), 2, 32, 2);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder var1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SelectionType getType() {
		return SelectionType.NONE;
	}

	@Override
	public void forEachChild(Consumer<ClickableWidget> var1) {
		// TODO Auto-generated method stub
		
	}

	public static final class Category {
		//private final Text text;
		private final List<Entry> entries;
		
		private Category(Text text) {
			//this.text = text;
			this.entries = new ArrayList<>();
		}
		
		public Category addEntry(Text text, Runnable onSelect) {
			this.entries.add(new Entry(text, onSelect));
			return this;
		}
	}
	private static final record Entry(Text text, Runnable onSelect) {
	}
}
