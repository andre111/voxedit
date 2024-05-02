package me.andre111.voxedit.client.gui.widget;

import me.andre111.voxedit.client.gui.Textures;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class StatusBarWidget extends ClickableWidget {
	private Text status;
	
	public StatusBarWidget(int x, int y, int width, int height) {
		super(x, y, width, height, Text.empty());
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		// background
		context.drawTexture(Textures.BACKGROUND, getX(), getY(), 0, 0, getWidth(), getHeight());
        context.drawTexture(Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE, getX(), getY()-2, 0.0f, 0.0f, getWidth(), 2, 32, 2);
		
        if(status != null) {
        	TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        	int textWidth = textRenderer.getWidth(status);
        	context.drawText(textRenderer, status, getRight() - textWidth - 8, getY() + 6, 0xFFFFFFFF, true);
        }
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
	}
	
	public void setStatus(Text status) {
		this.status = status;
	}
}