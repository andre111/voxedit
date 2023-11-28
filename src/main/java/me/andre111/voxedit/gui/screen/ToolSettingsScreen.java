package me.andre111.voxedit.gui.screen;

import java.util.List;

import me.andre111.voxedit.ClientState;
import me.andre111.voxedit.tool.Tool;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

public class ToolSettingsScreen extends Screen {
	private int contentWidth = 100;
	private int contentHeight = 96;
	private int padding = 2;
	
	private List<? extends ToolSetting<?, ?>> toolSettings;
	private Tool<?, ?> lastTool = null;

	public ToolSettingsScreen() {
		super(Text.of("Tool Settings"));
		rebuild();
	}
	
	public void rebuild() {
		if(ClientState.active == null) return;
		if(lastTool != ClientState.active.tool()) {
			lastTool = ClientState.active.tool();
			init(MinecraftClient.getInstance(), MinecraftClient.getInstance().getWindow().getScaledWidth(), MinecraftClient.getInstance().getWindow().getScaledHeight());
		} else {
			reload();
		}
	}
	
	private void reload() {
		if(ClientState.active == null) return;

		for(var setting : toolSettings) {
			setting.reload();
		}
	}
		

	@Override
	protected void init() {
		contentWidth = 100;
		contentHeight = 8;
		if(ClientState.active == null) return;

		toolSettings = ClientState.active.config().getSettings();
		contentHeight += toolSettings.size() * 22;
		
		int x = 2+padding;
		int y = (height-contentHeight-padding*2) / 2;
		
		int currentY = y;
		addDrawableChild(new TextWidget(x, currentY, contentWidth, 14, ClientState.active.tool().asText().copy().append(" Settings"), textRenderer).alignCenter());
		currentY += 12;
		for(var setting : toolSettings) {
			for(var e : setting.create(this, x, currentY, contentWidth, 20)) {
				addDrawableChild(e);
			}
			currentY += 22;
		}
	}
	
	@Override
	public void renderInGameBackground(DrawContext context) {
		int x = 2;
		int y = (context.getScaledWindowHeight()-contentHeight-padding*2) / 2;
        context.fillGradient(x, y, x + contentWidth + padding * 2, y + contentHeight + padding * 2, -1072689136, -804253680);
    }
}
