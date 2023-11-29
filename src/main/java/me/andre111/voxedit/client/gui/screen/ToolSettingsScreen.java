/*
 * Copyright (c) 2023 André Schweiger
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
package me.andre111.voxedit.client.gui.screen;

import java.util.ArrayList;
import java.util.List;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.client.ClientState;
import me.andre111.voxedit.client.gui.widget.ToolSettingWidget;
import me.andre111.voxedit.client.network.ClientNetworking;
import me.andre111.voxedit.tool.Tool;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

@Environment(value=EnvType.CLIENT)
public class ToolSettingsScreen extends Screen {
	private int contentWidth = 100;
	private int contentHeight = 96;
	private int padding = 2;
	
	private TextWidget toolName;
	private List<ToolSettingWidget<?, ?, ?>> toolSettingWidgets;
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
		for(var setting : toolSettingWidgets) {
			setting.reload();
		}
	}
		

	@Override
	protected void init() {
		contentWidth = 100;
		contentHeight = 8+12;
		if(ClientState.active == null) return;

		toolSettingWidgets = new ArrayList<>();
		for(var toolSetting : ClientState.active.selected().config().getSettings()) {
			toolSettingWidgets.add(ToolSettingWidget.of(toolSetting));
		}
		contentHeight += (toolSettingWidgets.size()+1) * 22;
		
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
				ClientNetworking.setTool(newTool.getDefault());
			});
		}).dimensions(x, currentY, contentWidth, 20).build());
		currentY += 22;
		
		// tool settings
		for(var setting : toolSettingWidgets) {
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
