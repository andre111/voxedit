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

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;

public class OverlayWidget extends ContainerWidget implements LayoutWidget {
	private Widget parent;
	private Widget overlay;

	public OverlayWidget(int x, int y, int width, int height) {
		super(x, y, width, height, Text.empty());
		visible = false;
	}
	
	public void openOverlay(Widget parent, Widget overlay) {
		closeOverlay();
		this.parent = parent;
		this.overlay = overlay;
		visible = true;
		refreshPositions();
	}
	
	public void closeOverlay() {
		parent = null;
		overlay = null;
		visible = false;
	}
	
    @Override
    public boolean isFocused() {
		return visible;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if(!visible) return false;
		if(super.mouseClicked(mouseX, mouseY, button)) return true;
		//if(clicked(mouseX, mouseY)) {
			closeOverlay();
			return true;
		//}
		//return false;
	}

	@Override
	public List<? extends Element> children() {
		if(overlay instanceof Element e) {
			return List.of(e);
		} else if(overlay instanceof LayoutWidget l) {
			List<Element> list = new ArrayList<>();
			l.forEachElement(w -> { if(w instanceof Element e) list.add(e); });
			return list;
		} else {
			return List.of();
		}
	}

	@Override
	public void forEachElement(Consumer<Widget> consumer) {
		if(overlay != null) consumer.accept(overlay);
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		context.getMatrices().translate(0, 0, 10);
		context.fill(getX(), getY(), getX()+getWidth(), getY()+getHeight(), 0x88000000);
		
		if(parent instanceof Drawable drawable) drawable.render(context, mouseX, mouseY, delta);
		else if(parent instanceof LayoutWidget layout) layout.forEachChild(w -> w.render(context, mouseX, mouseY, delta));
		
		if(overlay instanceof Drawable drawable) drawable.render(context, mouseX, mouseY, delta);
		else if(overlay instanceof LayoutWidget layout) layout.forEachChild(w -> w.render(context, mouseX, mouseY, delta));
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
	}

	@Override
	public void refreshPositions() {
		if(parent != null && overlay != null) {
			int x = parent.getX();
			int y = parent.getY() + parent.getHeight();
			int w = overlay.getWidth();
			int h = overlay.getHeight();
			
			if(x < getX()) x = getX();
			if(y < getY()) y = getY();
			if(x + w > getWidth()) x = getX() + getWidth() - w;
			if(y + h > getHeight()) y = getY() + getHeight() - h;
			
			overlay.setPosition(x, y);
		}
		LayoutWidget.super.refreshPositions();
	}
}
