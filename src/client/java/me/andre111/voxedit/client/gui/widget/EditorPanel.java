package me.andre111.voxedit.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;

public class EditorPanel extends ContainerWidget implements LayoutWidget {
	protected final EditorWidget parent;
	private Location location;
	protected int gap;
	
	protected List<ClickableWidget> children;

	public EditorPanel(EditorWidget parent, Location location, Text text) {
		super(0, 0, 100, 100, text);
		this.parent = parent;
		this.location = location;
		this.gap = 2;
		this.children = new ArrayList<>();
	}
	
	public void addChild(ClickableWidget child) {
		children.add(child);
		refreshPositions();
		parent.refreshPositions();
	}

	@Override
	public List<? extends Element> children() {
		return children;
	}

	@Override
	public void forEachElement(Consumer<Widget> consumer) {
		children.forEach(consumer);
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawTexture(Screen.INWORLD_HEADER_SEPARATOR_TEXTURE, getX(), getY(), 0.0f, 0.0f, width, 2, 32, 2);
		context.drawTexture(EditorWidget.BACKGROUND_TEXTURE, getX(), getY()+2, 0, 0, width, 24-4);
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, getMessage(), getX()+width/2, getY()+8, 0xFFFFFFFF);
        context.drawTexture(Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE, getX(), getY()+24-2, 0.0f, 0.0f, width, 2, 32, 2);
		
		for(var child : children) {
			child.render(context, mouseX, mouseY, delta);
		}
		
        context.drawTexture(Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE, getX(), getY()+height-2, 0.0f, 0.0f, width, 2, 32, 2);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void refreshPositions() {
		int x = getX();
		int y = getY() + 28;
		int maxHeight = getY() + 28;
		for(var child : children) {
			int childWidth = child.getWidth();
			int childHeight = child.getHeight();
			if(x + childWidth > width) {
				x = 0;
				y = maxHeight + gap;
			}
			child.setPosition(x, y);
			x += childWidth + gap;
			maxHeight = Math.max(maxHeight, y + childHeight);
		}
		height = Math.max(32, maxHeight + 4) - getY();

		LayoutWidget.super.refreshPositions();
	}

	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		if(location == this.location) return;
		this.location = location;
		parent.refreshPositions();
	}
	
	public static enum Location {
		LEFT,
		RIGHT;
	}
}
