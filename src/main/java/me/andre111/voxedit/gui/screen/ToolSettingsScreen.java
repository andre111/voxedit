package me.andre111.voxedit.gui.screen;

import java.util.List;

import me.andre111.voxedit.ClientState;
import me.andre111.voxedit.Networking;
import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.tool.Tool;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class ToolSettingsScreen extends Screen {
	private int contentWidth = 100;
	private int contentHeight = 96;
	private int padding = 2;
	
	private TextWidget toolName;
	private List<? extends ToolSetting<?, ?>> toolSettings;
	private Tool<?, ?> lastTool = null;

	public ToolSettingsScreen() {
		super(Text.of("Tool Settings"));
		rebuild();
	}
	
	public void rebuild() {
		if(ClientState.active == null) return;
		if(lastTool != ClientState.active.selected().tool()) {
			lastTool = ClientState.active.selected().tool();
			init(MinecraftClient.getInstance(), MinecraftClient.getInstance().getWindow().getScaledWidth(), MinecraftClient.getInstance().getWindow().getScaledHeight());
		} else {
			reload();
		}
	}
	
	private void reload() {
		if(ClientState.active == null) return;

		// (re)load name display
		MutableText name = Text.of("Tool: ").copy().append(ClientState.active.selected().tool().asText());
		if(ClientState.active.size() > 1) {
			name = name.append(" ("+(ClientState.active.selectedIndex()+1)+"/"+ClientState.active.size()+")");
		}
		toolName.setMessage(name);
		
		// reload settings
		for(var setting : toolSettings) {
			setting.reload();
		}
	}
		

	@Override
	protected void init() {
		contentWidth = 100;
		contentHeight = 8+12;
		if(ClientState.active == null) return;

		toolSettings = ClientState.active.selected().config().getSettings();
		contentHeight += (toolSettings.size()+1) * 22;
		
		int x = 2+padding;
		int y = (height-contentHeight-padding*2) / 2;
		
		// title
		int currentY = y;
		addDrawableChild(new TextWidget(x, currentY, contentWidth, 14, Text.of("Tool Settings"), textRenderer).alignCenter());
		currentY += 12;
		addDrawableChild(toolName = new TextWidget(x, currentY, contentWidth, 14, Text.of(""), textRenderer).alignCenter());
		currentY += 12;
		
		// change tool button
		addDrawableChild(ButtonWidget.builder(Text.of("Change Tool"), (button) -> {
			InputScreen.getSelector(this, Text.of("Change Tool - Will reset other settings!"), Text.of("Tool"), ClientState.active.selected().tool(), VoxEdit.TOOL_REGISTRY.stream().toList(), Tool::asText, (newTool) -> {
				Networking.clientSendToolChange(newTool.getDefault());
			});
		}).dimensions(x, currentY, contentWidth, 20).build());
		currentY += 22;
		
		// tool settings
		for(var setting : toolSettings) {
			for(var e : setting.create(this, x, currentY, contentWidth, 20)) {
				addDrawableChild(e);
			}
			currentY += 22;
		}
		
		reload();
	}
	
	@Override
	public void renderInGameBackground(DrawContext context) {
		int x = 2;
		int y = (context.getScaledWindowHeight()-contentHeight-padding*2) / 2;
        context.fillGradient(x, y, x + contentWidth + padding * 2, y + contentHeight + padding * 2, -1072689136, -804253680);
    }
	
	@Override
	public void close() {
		setFocused(null);
		super.close();
	}
}
