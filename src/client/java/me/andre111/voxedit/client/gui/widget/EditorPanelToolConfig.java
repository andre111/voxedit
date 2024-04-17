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

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.client.EditorState;
import me.andre111.voxedit.client.gui.screen.InputScreen;
import me.andre111.voxedit.tool.Tool;
import me.andre111.voxedit.tool.data.ToolConfig;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class EditorPanelToolConfig extends EditorPanel {
	private List<ToolSettingWidget<?, ?>> toolSettingWidgets = new ArrayList<>();
	private SelectionWidget<String> presets;
	private boolean rebuilding = false;
	private boolean reloading = false;

	public EditorPanelToolConfig(EditorWidget parent) {
		super(parent, VoxEdit.id("tool_config"), Text.translatable("voxedit.screen.panel.toolConfig"));
		
		EditorState.CHANGE_TOOL.register((tool) -> rebuild());
		EditorState.CHANGE_TOOL_CONFIG.register((toolConfig) -> reload());
	}

	private void rebuild() {
		if(rebuilding) return;
		
		rebuilding = true;
		clearContent();
		toolSettingWidgets.clear();
		
		Tool tool = EditorState.tool();
		if(tool != null && !tool.getSettings().isEmpty()) {
			// presets / saved configs
			presets = new SelectionWidget<>(width, (width - 4*2)/3, 32, null, this::setPreset);
			presets.setPadding(2);
			presets.setGap(2);
			for(var e : EditorState.persistant().presets(tool).entrySet()) {
				presets.addOption(e.getKey(), Text.of(e.getKey()));
			}
			presets.withAdditionalButton(Text.of("+"), () -> true, this::savePreset);
			presets.withAdditionalButton(Text.of("-"), () -> presets.getValue() != null, this::deletePreset);
			addContent(presets);
			addContent(new LineHorizontal(width));
			
			// settings
			for(var toolSetting : tool.getSettings()) {
				ToolSettingWidget<?, ?> toolSettingWidget = ToolSettingWidget.of(toolSetting, () -> EditorState.toolConfig(), (config) -> {
					presets.setValue(null);
					EditorState.toolConfig(config);
				});
				toolSettingWidgets.add(toolSettingWidget);
				
				for(ClickableWidget widget : toolSettingWidget.create(parent.getScreen(), 0, 0, width, 20)) {
					addContent(widget);
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
	
	private void setPreset(String name) {
		if(name == null || name.isBlank()) return;
		Tool tool = EditorState.tool();
		if(tool == null) return;
		ToolConfig config = EditorState.persistant().presets(tool).get(name);
		if(config == null || !tool.isValid(config)) return;
		
		EditorState.toolConfig(config);
	}
	
	private void savePreset() {
		InputScreen.getString(parent.getScreen(), Text.translatable("voxedit.prompt.preset.name"), "", (name) -> {
			if(name == null || name.isBlank()) return;
			Tool tool = EditorState.tool();
			if(tool == null) return;
			ToolConfig config = EditorState.toolConfig();
			if(config == null || !tool.isValid(config)) return;
			
			if(EditorState.persistant().presets(tool).containsKey(name)) {
				InputScreen.showConfirmation(parent.getScreen(), Text.translatable("voxedit.prompt.preset.override", name), () -> {
					EditorState.persistant().preset(tool, name, config);
					rebuild();
				});
			} else {
				EditorState.persistant().preset(tool, name, config);
				rebuild();
			}
		});
	}
	
	private void deletePreset() {
		String name = presets.getValue();
		if(name == null) return;
		
		InputScreen.showConfirmation(parent.getScreen(), Text.translatable("voxedit.prompt.preset.delete", name), () -> {
			Tool tool = EditorState.tool();
			if(tool == null) return;
			
			EditorState.persistant().deletePreset(tool, name);
			rebuild();
		});
	}
}
