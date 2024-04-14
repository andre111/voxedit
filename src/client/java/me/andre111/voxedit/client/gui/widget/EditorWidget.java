package me.andre111.voxedit.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.mojang.blaze3d.systems.RenderSystem;

import me.andre111.voxedit.client.gui.Textures;
import me.andre111.voxedit.client.gui.screen.EditorScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;

public class EditorWidget extends ContainerWidget implements LayoutWidget {
    private final EditorScreen screen;
    private final MenuWidget menu;
	private final List<EditorPanel> panels = new ArrayList<>();
	
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
	
	public EditorPanel addPanel(EditorPanel.Location location, Text text) {
		EditorPanel panel = new EditorPanel(this, location, text);
		panels.add(panel);
		return panel;
	}
	
	public void addPanel(Function<EditorWidget, EditorPanel> creator) {
		panels.add(creator.apply(this));
	}

	@Override
	public List<? extends Element> children() {
		//TODO: improve
		List<Element> children = new ArrayList<>();
		children.add(menu);
		children.addAll(panels);
		return children;
	}

	@Override
	public void forEachElement(Consumer<Widget> consumer) {
		consumer.accept(menu);
		panels.forEach(consumer);
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
		
		for(EditorPanel panel : panels) {
			panel.render(context, mouseX, mouseY, delta);
		}
		menu.render(context, mouseX, mouseY, delta);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		for(EditorPanel panel : panels) panel.appendClickableNarrations(builder);
	}

	@Override
	public void refreshPositions() {
		menu.setWidth(screen.width);
		
		for(EditorPanel panel : panels) {
			if(panel.getLocation() == EditorPanel.Location.LEFT) {
				panel.setWidth(leftWidth);
			} else {
				panel.setWidth(rightWidth);
			}
		}
		
		LayoutWidget.super.refreshPositions();
		
		int leftY = menu.getHeight();
		int rightY = menu.getHeight();
		for(EditorPanel panel : panels) {
			if(panel.getLocation() == EditorPanel.Location.LEFT) {
				panel.setPosition(0, leftY);
				leftY += panel.getHeight();
			} else {
				panel.setPosition(width-rightWidth, rightY);
				rightY += panel.getHeight();
			}
		}
		
		LayoutWidget.super.refreshPositions();
	}
	
	public boolean isOverGui(double mouseX, double mouseY) {
		if(mouseY <= menu.getHeight()) return true;
		if(mouseX <= leftWidth) return true;
		if(mouseX >= width-rightWidth) return true;
		return false;
	}
}
