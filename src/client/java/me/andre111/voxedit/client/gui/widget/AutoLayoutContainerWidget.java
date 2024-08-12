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
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;

public class AutoLayoutContainerWidget extends ContainerWidget implements LayoutWidget {
	protected final Element parent;
	protected final List<Element> children = new ArrayList<>();
	
	protected int paddingTop = 0;
	protected int paddingBottom = 0;
	protected int paddingLeft = 0;
	protected int paddingRight = 0;
	protected int gapX = 0;
	protected int gapY = 0;
	
	public AutoLayoutContainerWidget(Element parent, int x, int y, int width, int height, Text message) {
		super(x, y, width, height, message);
		
		this.parent = parent;
	}
	
	public void addChild(Element child) {
		if(child == this) throw new IllegalArgumentException("Trying to add container as child of itself.");
		children.add(child);
	}

	@Override
	public List<? extends Element> children() {
		return children;
	}

	@Override
	public void forEachElement(Consumer<Widget> consumer) {
		for(Element child : children) {
			if(child instanceof Widget widget) consumer.accept(widget);
		}
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		for(Element child : children) {
			if(child instanceof Drawable drawable) drawable.render(context, mouseX, mouseY, delta);
		}
	}

	@Override
	public void appendClickableNarrations(NarrationMessageBuilder var1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refreshPositions() {
		int oldWidth = width;
		int oldHeight = height;
		
		int x = getX() + paddingLeft;
		int y = getY() + paddingTop;
		int maxY = y;
		for(var child : children) {
			if(child instanceof Widget widget) {
				if(widget instanceof ClickableWidget cw && !cw.visible) continue;
				
				int childWidth = widget.getWidth();
				int childHeight = widget.getHeight();
				if(x + childWidth > (getX()+width)-paddingRight) {
					x = getX() + paddingLeft;
					y = maxY + gapY;
				}
				widget.setPosition(x, y);
				x += childWidth + gapX;
				maxY = Math.max(maxY, y + childHeight);
			}
		}
		height = (maxY - getY()) + paddingBottom;

		LayoutWidget.super.refreshPositions();
		
		if(oldWidth != width || oldHeight != height) {
			if(parent instanceof LayoutWidget layoutParent) {
				layoutParent.refreshPositions();
			}
		}
	}
}
