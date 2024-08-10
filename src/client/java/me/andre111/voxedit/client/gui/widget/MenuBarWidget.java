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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.AxisGridWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;

public class MenuBarWidget extends ContainerWidget implements LayoutWidget {
	private final OverlayWidget overlayWidget;
	private final List<Category> categories = new ArrayList<>();
	
	public MenuBarWidget(int x, int y, int width, int height, OverlayWidget overlayWidget) {
		super(x, y, width, height, Text.empty());
		this.overlayWidget = overlayWidget;
	}
	
	public Category addCategory(Text text) {
		Category category = new Category(this, text);
		categories.add(category);
		return category;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		// background
		context.drawTexture(Textures.BACKGROUND, getX(), getY(), 0, 0, getWidth(), getHeight());
        context.drawTexture(Screen.INWORLD_HEADER_SEPARATOR_TEXTURE, getX(), getY()+getHeight(), 0.0f, 0.0f, getWidth(), 2, 32, 2);

		for(Category category : categories) {
			category.button.render(context, mouseX, mouseY, delta);
		}
	}
	
	@Override
	public List<? extends Element> children() {
		return categories.stream().map(c -> c.button).toList();
	}
	
	@Override
	public void forEachElement(Consumer<Widget> consumer) {
		for(Category category : categories) {
			consumer.accept(category.button);
		}
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder var1) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void refreshPositions() {
		int x = getX();
		int y = getY();
		for(Category category : categories) {
			category.button.setX(x);
			category.button.setY(y);
			category.button.setHeight(height);
			category.button.setWidth(MinecraftClient.getInstance().textRenderer.getWidth(category.button.getMessage())+8);
			x += category.button.getWidth();
		}
		LayoutWidget.super.refreshPositions();
	}

	public static final class Category {
		private final MenuBarWidget menu;
		private final ButtonWidget button;
		private final List<Entry> entries;
		private AxisGridWidget entriesWidget;
		
		private Category(MenuBarWidget menu, Text text) {
			this.menu = menu;
			this.button = ButtonWidget.builder(text, (b) -> {
					menu.overlayWidget.openOverlay(getButton(), getEntriesWidget(), false);
				}).width(MinecraftClient.getInstance().textRenderer.getWidth(text)+8).build();
			this.entries = new ArrayList<>();
		}
		
		public Category addEntry(Text text, Runnable onSelect) {
			this.entries.add(new Entry(text, onSelect));
			entriesWidget = null;
			return this;
		}
		
		private Widget getButton() {
			return button;
		}
		
		private Widget getEntriesWidget() {
			if(entriesWidget == null) {
				entriesWidget = new AxisGridWidget(200, entries.size()*20, AxisGridWidget.DisplayAxis.VERTICAL);
				for(Entry entry : entries) {
					entriesWidget.add(ButtonWidget.builder(entry.text, (b) -> {
						menu.overlayWidget.closeOverlay();
						entry.onSelect.run(); 
					}).size(200, 20).build());
				}
			}
			return entriesWidget;
		}
	}
	private static final record Entry(Text text, Runnable onSelect) {
	}
}
