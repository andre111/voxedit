package me.andre111.voxedit.client.gui.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class LineHorizontal extends ClickableWidget {
	public LineHorizontal(int width) {
		super(0, 0, width, 2 + 2*2, Text.empty());
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		context.drawTexture(Screen.INWORLD_HEADER_SEPARATOR_TEXTURE, getX(), getY()+2, 0.0f, 0.0f, width, 2, 32, 2);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
	}
}