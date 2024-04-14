package me.andre111.voxedit.client.gui.widget;

import java.util.ArrayList;
import java.util.List;

import me.andre111.voxedit.client.EditorState;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class EditorPanelToolConfig extends EditorPanel {
	private List<ToolSettingWidget<?, ?>> toolSettingWidgets = new ArrayList<>();
	private boolean rebuilding = false;
	private boolean reloading = false;

	public EditorPanelToolConfig(EditorWidget parent, Location location) {
		super(parent, location, Text.translatable("voxedit.screen.panel.toolConfig"));
		
		EditorState.CHANGE_TOOL.register((tool) -> rebuild());
		EditorState.CHANGE_TOOL_CONFIG.register((toolConfig) -> reload());
	}

	private void rebuild() {
		if(rebuilding) return;
		
		rebuilding = true;
		children.clear();
		toolSettingWidgets.clear();
		
		if(EditorState.tool() != null) {
			// presets / saved configs
			for(var preset : EditorState.persistant().presets(EditorState.tool())) {
				children.add(ButtonWidget.builder(Text.of(preset.name()), (button) -> EditorState.toolConfig(preset.config())).size(64, 32).build());
			}
			children.add(new LineHorizontal(width));
			
			// settings
			for(var toolSetting : EditorState.tool().getSettings()) {
				ToolSettingWidget<?, ?> toolSettingWidget = ToolSettingWidget.of(toolSetting, () -> EditorState.toolConfig(), (config) -> EditorState.toolConfig(config));
				toolSettingWidgets.add(toolSettingWidget);
				
				for(ClickableWidget widget : toolSettingWidget.create(parent.getScreen(), 0, 0, width, 20)) {
					children.add(widget);
				}
			}
			reload();
		}
		
		parent.refreshPositions();
		rebuilding = false;
	}
	
	private void reload() {
		if(rebuilding) return;
		if(reloading) return;
		
		reloading = true;
		for(var toolSettingWidget : toolSettingWidgets) {
			toolSettingWidget.reload();
		}
		reloading = false;
	}
}
