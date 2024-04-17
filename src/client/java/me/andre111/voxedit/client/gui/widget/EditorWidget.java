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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.mojang.blaze3d.systems.RenderSystem;

import me.andre111.voxedit.client.EditorLayout;
import me.andre111.voxedit.client.gui.Textures;
import me.andre111.voxedit.client.gui.screen.EditorScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EditorWidget extends ContainerWidget implements LayoutWidget {
    private final EditorScreen screen;
    private final MenuWidget menu;
	private final Map<Location, List<EditorPanel>> panels = new HashMap<>();
	private final Map<Identifier, EditorPanel> panelMap = new HashMap<>();
	
	private int leftWidth = 300;
	private int rightWidth = 300;

	public EditorWidget(EditorScreen screen) {
		super(0, 0, 100, 100, Text.empty());
		
		this.screen = screen;
		this.menu = new MenuWidget(0, 0, screen.width, 20);
	}
	
	public EditorScreen getScreen() {
		return screen;
	}
	
	public MenuWidget getMenu() {
		return menu;
	}
	
	public void addPanel(Function<EditorWidget, EditorPanel> creator, Location location) {
		addPanel(creator.apply(this), location);
	}
	
	private void addPanel(EditorPanel panel, Location location) {
		getPanels(location).add(panel);
		panelMap.put(panel.getID(), panel);
	}
	
	private List<EditorPanel> getPanels(Location location) {
		return panels.computeIfAbsent(location, loc -> new ArrayList<>());
	}
	
	public void loadLayout(EditorLayout layout) {
		// track existing panels + their old locations
		List<EditorPanel> remainingPanels = new ArrayList<>();
		Map<EditorPanel, Location> oldLocations = new HashMap<>();
		for(var e : panels.entrySet()) {
			remainingPanels.addAll(e.getValue());
			for(EditorPanel panel : e.getValue()) oldLocations.put(panel, e.getKey());
		}
		
		// load from layout
		panels.clear();
		for(EditorLayout.PanelLocation panelLocation : layout.panelLocations()) {
			EditorPanel panel = panelMap.get(panelLocation.panel());
			if(!remainingPanels.contains(panel)) continue;
			remainingPanels.remove(panel);
			
			if(panel != null) {
				addPanel(panel, panelLocation.location());
			}
		}
		
		// add panels with missing entries at their old location
		for(EditorPanel remaining : remainingPanels) {
			addPanel(remaining, oldLocations.getOrDefault(remaining, Location.LEFT));
		}
		
		refreshPositions();
	}
	
	public EditorLayout getLayout() {
		List<EditorLayout.PanelLocation> panelLocations = new ArrayList<>();
		for(var e : panels.entrySet()) {
			for(EditorPanel panel : e.getValue()) panelLocations.add(new EditorLayout.PanelLocation(panel.getID(), e.getKey()));
		}
		return new EditorLayout(panelLocations);
	}
	
	public void onLayoutChange() {
		refreshPositions();
		screen.onLayoutChange();
	}
	
	public Location getLocation(EditorPanel panel) {
		for(var e : panels.entrySet()) {
			if(e.getValue().contains(panel)) return e.getKey();
		}
		return Location.LEFT;
	}
	
	public void setLocation(EditorPanel panel, Location location) {
		Location oldLocation = getLocation(panel);
		if(oldLocation == location) return;
		
		getPanels(oldLocation).remove(panel);
		getPanels(location).add(panel);
		onLayoutChange();
	}
	
	public boolean isFirst(EditorPanel panel) {
		return getPanels(getLocation(panel)).indexOf(panel) == 0;
	}
	
	public boolean isLast(EditorPanel panel) {
		List<EditorPanel> list = getPanels(getLocation(panel));
		return list.indexOf(panel) == list.size()-1;
	}
	
	public void moveUp(EditorPanel panel) {
		List<EditorPanel> list = getPanels(getLocation(panel));
		
		int index = list.indexOf(panel);
		if(index > 0) {
			list.remove(panel);
			list.add(index-1, panel);
			onLayoutChange();
		}
	}
	
	public void moveDown(EditorPanel panel) {
		List<EditorPanel> list = getPanels(getLocation(panel));
		
		int index = list.indexOf(panel);
		if(index < list.size()-1) {
			list.remove(panel);
			list.add(index+1, panel);
			onLayoutChange();
		}
	}
	
	public void moveToBottom(EditorPanel panel) {
		List<EditorPanel> list = getPanels(getLocation(panel));
		
		if(list.contains(panel)) {
			list.remove(panel);
			list.add(panel);
			onLayoutChange();
		}
	}

	@Override
	public List<? extends Element> children() {
		//TODO: improve
		List<Element> children = new ArrayList<>();
		for(var list : panels.values()) children.addAll(list);
		children.add(menu);
		return children;
	}

	@Override
	public void forEachElement(Consumer<Widget> consumer) {
		for(var list : panels.values()) list.forEach(consumer);
		consumer.accept(menu);
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		RenderSystem.enableBlend();
		for(int i=0; i<2; i++) {
			context.drawTexture(Textures.BACKGROUND, 0, menu.getHeight(), 0, 0, leftWidth+1, context.getScaledWindowHeight());
			context.drawTexture(Textures.BACKGROUND, context.getScaledWindowWidth()-rightWidth-1, menu.getHeight(), 0, 0, rightWidth+1, context.getScaledWindowHeight());
		}
		RenderSystem.disableBlend();
		
		context.drawVerticalLine(leftWidth+1, menu.getHeight(), context.getScaledWindowHeight(), 0xFFFFFFFF);
		context.drawVerticalLine(context.getScaledWindowWidth()-rightWidth-1, menu.getHeight(), context.getScaledWindowHeight(), 0xFFFFFFFF);
		
		for(var list : panels.values()) {
			for(EditorPanel panel : list) {
				panel.render(context, mouseX, mouseY, delta);
			}
		}
		menu.render(context, mouseX, mouseY, delta);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		for(var list : panels.values()) {
			for(EditorPanel panel : list) {
				panel.appendClickableNarrations(builder);
			}
		}
	}

	@Override
	public void refreshPositions() {
		menu.setWidth(screen.width);

		for(EditorPanel panel : getPanels(Location.LEFT)) panel.setWidth(leftWidth);
		for(EditorPanel panel : getPanels(Location.RIGHT)) panel.setWidth(rightWidth);
		
		LayoutWidget.super.refreshPositions();
		
		int leftY = menu.getHeight();
		for(EditorPanel panel : getPanels(Location.LEFT)) {
			panel.setPosition(0, leftY);
			leftY += panel.getHeight();
		}
		
		int rightY = menu.getHeight();
		for(EditorPanel panel : getPanels(Location.RIGHT)) {
			panel.setPosition(width-rightWidth, rightY);
			rightY += panel.getHeight();
		}
		
		LayoutWidget.super.refreshPositions();
	}
	
	public boolean isOverGui(double mouseX, double mouseY) {
		if(mouseY <= menu.getHeight()) return true;
		if(mouseX <= leftWidth) return true;
		if(mouseX >= width-rightWidth) return true;
		return false;
	}

	public static enum Location {
		LEFT,
		RIGHT;
	}
}
