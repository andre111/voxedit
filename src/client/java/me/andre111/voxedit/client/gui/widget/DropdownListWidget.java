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

import java.util.List;
import java.util.function.Consumer;

import me.andre111.voxedit.client.gui.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

public class DropdownListWidget extends ButtonWidget {
	private final List<String> options;
	private final Consumer<String> onChange;
	private final OverlayWidget overlay;
	private String value;

	public DropdownListWidget(int x, int y, int width, int height, Text message, String value, List<String> options, Consumer<String> onChange, OverlayWidget overlay) {
		super(x, y, width, height, message, (button) -> {}, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
		this.options = List.copyOf(options);
		this.onChange = onChange;
		this.overlay = overlay;
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public void onPress() {
		System.out.println("open");
		overlay.openOverlay(this, new SelectionListWidget(width-16, 400, 0, 4, options, selected -> {
			if(selected != null && !selected.equals(value)) {
				value = selected;
				onChange.accept(value);
				overlay.closeOverlay();
			}
		}), true);
	}
	
	@Override
	public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
		int margin = 4;
		int iconSize = getHeight() - margin * 2;
		
		int xMin = getX() + margin;
        int xMax = getX() + getWidth() - margin*2 - iconSize;
        ClickableWidget.drawScrollableText(context, textRenderer, getMessage().copy().append(value), xMin, getY(), xMax, getY() + getHeight(), color);
        
        context.drawGuiTexture(Textures.MOVE_DOWN, getX() + getWidth() - margin - iconSize, getY() + margin, iconSize, iconSize);
	}
	
	private static class SelectionListWidget extends ModListWidget<SelectionListWidget.SelectionListEntry> {
		private final List<String> options;
		private final Consumer<String> onSelect;
		
		public SelectionListWidget(int width, int height, int y, int padding, List<String> options, Consumer<String> onSelect) {
			super(MinecraftClient.getInstance(), width, height, y, padding);
			this.options = options;
			this.onSelect = onSelect;
			
			for(String option : options) {
				addEntry(new SelectionListEntry(option));
			}
		}
		
		@Override
		public void setSelected(int selectedIndex) {
	        super.setSelected(selectedIndex);
	        if(selectedIndex >= 0 && selectedIndex < options.size()) {
	        	onSelect.accept(options.get(selectedIndex));
	        }
	    }
		
		@Override
	    protected void renderDecorations(DrawContext context, int mouseX, int mouseY) {
			context.drawBorder(getX(), getY()-1, width, height+1, 0xFFFFFFFF);
	    }

		@Environment(value=EnvType.CLIENT)
		class SelectionListEntry extends ModListWidget.Entry<SelectionListEntry> {
			private final TextWidget textWidget;
			private final List<TextWidget> children;
			
			SelectionListEntry(String text) {
				textWidget = new TextWidget(SelectionListWidget.this.getRowWidth(), 20, Text.of(text), MinecraftClient.getInstance().textRenderer);
				children = List.of(textWidget);
			}

			@Override
			public List<? extends Element> children() {
				return children;
			}

			@Override
			public int getHeight() {
				return 20;
			}

			@Override
			public void positionChildren() {
				textWidget.setPosition(getX(), getY());
			}

			@Override
			protected void appendClickableNarrations(NarrationMessageBuilder var1) {
			}
		}
	}
}
