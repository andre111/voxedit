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
package me.andre111.voxedit.client.gui.widget.editor;

import java.util.ArrayList;
import java.util.List;

import me.andre111.voxedit.client.gui.Textures;
import me.andre111.voxedit.client.gui.widget.AutoLayoutContainerWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EditorPanel extends AutoLayoutContainerWidget {
	protected final EditorWidget parent;
	protected final Identifier id;

	private LayoutButton buttonMoveRight;
	private LayoutButton buttonMoveLeft;
	private LayoutButton buttonMoveUp;
	private LayoutButton buttonMoveDown;
	private List<ClickableWidget> buttons;
	private List<ClickableWidget> content;

	public EditorPanel(EditorWidget parent, Identifier id, Text text) {
		super(parent, 0, 0, 100, 100, text);
		this.parent = parent;
		this.id = id;
		this.buttons = new ArrayList<>();
		this.content = new ArrayList<>();

		paddingTop = 28;
		paddingBottom = 4;
		gapX = 2;
		gapY = 2;
		
		init();
	}

	private void init() {
		buttons.add(buttonMoveRight = new LayoutButton(Textures.BUTTON_MOVE_RIGHT, () -> parent.setLocation(this, EditorWidget.Location.RIGHT)));
		buttons.add(buttonMoveLeft = new LayoutButton(Textures.BUTTON_MOVE_LEFT, () -> parent.setLocation(this, EditorWidget.Location.LEFT)));
		buttons.add(buttonMoveUp = new LayoutButton(Textures.BUTTON_MOVE_UP, () -> parent.moveUp(this)));
		buttons.add(buttonMoveDown = new LayoutButton(Textures.BUTTON_MOVE_DOWN, () -> parent.moveDown(this)));
		children.addAll(buttons);
	}

	public void addContent(ClickableWidget child) {
		content.add(child);
		children.add(child);
		refreshPositions();
	}
	
	public void addContent(List<? extends ClickableWidget> newChildren) {
		content.addAll(newChildren);
		children.addAll(newChildren);
		refreshPositions();
	}

	public void clearContent() {
		children.removeAll(content);
		content.clear();
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		context.drawTexture(Screen.INWORLD_HEADER_SEPARATOR_TEXTURE, getX(), getY(), 0.0f, 0.0f, width, 2, 32, 2);
		context.drawTexture(Textures.BACKGROUND, getX(), getY()+2, 0, 0, width, 24-4);
		context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, getMessage(), getX()+width/2, getY()+8, 0xFFFFFFFF);
		context.drawTexture(Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE, getX(), getY()+24-2, 0.0f, 0.0f, width, 2, 32, 2);
		
		super.renderWidget(context, mouseX, mouseY, delta);

		context.drawTexture(Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE, getX(), getY()+height-2, 0.0f, 0.0f, width, 2, 32, 2);
	}

	@Override
	public void refreshPositions() {
		// buttons
		if(parent.getLocation(this) == EditorWidget.Location.LEFT) {
			buttonMoveRight.active = true;
			buttonMoveRight.setPosition(getX()+getWidth()-24*1, getY());
			buttonMoveDown.setPosition(getX()+getWidth()-24*2, getY());
			buttonMoveUp.setPosition(getX()+getWidth()-24*3, getY());
			
			buttonMoveLeft.active = false;
		} else {
			buttonMoveLeft.active = true;
			buttonMoveLeft.setPosition(getX()+24*0, getY());
			buttonMoveUp.setPosition(getX()+24*1, getY());
			buttonMoveDown.setPosition(getX()+24*2, getY());
			
			buttonMoveRight.active = false;
		}
		buttonMoveUp.active = !parent.isFirst(this);
		buttonMoveDown.active = !parent.isLast(this);
		
		//TODO: this is kinda hacky
		// layout content but not buttons
		for(var button : buttons) button.visible = false;
		super.refreshPositions();
		for(var button : buttons) button.visible = true;
	}

	public Identifier getID() {
		return id;
	}
	
	private static class LayoutButton extends ButtonWidget {
		private final ButtonTextures textures;

		protected LayoutButton(ButtonTextures textures, Runnable onPress) {
			super(0, 0, 24, 24, Text.empty(), (button) -> onPress.run(), null);

			this.textures = textures;
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			Identifier texture = textures.get(active, isSelected());
			if(texture != null) context.drawGuiTexture(texture, getX(), getY(), getWidth(), getHeight());
		}
	}
}
