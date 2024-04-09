package me.andre111.voxedit.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.mojang.blaze3d.systems.RenderSystem;

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
    public static final Identifier BACKGROUND_TEXTURE = new Identifier("textures/gui/inworld_menu_background.png");
    
    private final EditorScreen screen;
	private final List<EditorPanel> panels = new ArrayList<>();
	
	private int leftWidth = 300;
	private int rightWidth = 300;

	public EditorWidget(EditorScreen screen) {
		super(0, 0, 100, 100, Text.empty());
		
		this.screen = screen;
	}
	
	public EditorScreen getScreen() {
		return screen;
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
		return panels;
	}

	@Override
	public void forEachElement(Consumer<Widget> consumer) {
		panels.forEach(consumer);
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		RenderSystem.enableBlend();
		for(int i=0; i<2; i++) {
			context.drawTexture(BACKGROUND_TEXTURE, 0, 0, 0, 0, leftWidth, context.getScaledWindowHeight());
			context.drawTexture(BACKGROUND_TEXTURE, context.getScaledWindowWidth()-rightWidth, 0, 0, 0, rightWidth, context.getScaledWindowHeight());
		}
		RenderSystem.disableBlend();
		
		context.drawVerticalLine(leftWidth, 0, context.getScaledWindowHeight(), 0xFFFFFFFF);
		context.drawVerticalLine(context.getScaledWindowWidth()-rightWidth, 0, context.getScaledWindowHeight(), 0xFFFFFFFF);
		
		for(EditorPanel panel : panels) {
			panel.render(context, mouseX, mouseY, delta);
		}
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		for(EditorPanel panel : panels) panel.appendClickableNarrations(builder);
	}

	@Override
	public void refreshPositions() {
		for(EditorPanel panel : panels) {
			if(panel.getLocation() == EditorPanel.Location.LEFT) {
				panel.setWidth(leftWidth);
			} else {
				panel.setWidth(rightWidth);
			}
		}
		
		LayoutWidget.super.refreshPositions();
		
		int leftY = 0;
		int rightY = 0;
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
		if(mouseX <= leftWidth) return true;
		if(mouseX >= width-rightWidth) return true;
		return false;
	}
}
