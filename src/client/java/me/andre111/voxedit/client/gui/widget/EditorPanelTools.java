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

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.client.EditorState;
import me.andre111.voxedit.tool.Tool;
import net.minecraft.text.Text;

public class EditorPanelTools extends EditorPanel {
	private final SelectionWidget<Tool> selection;

	public EditorPanelTools(EditorWidget parent) {
		super(parent, VoxEdit.id("tools"), Text.translatable("voxedit.screen.panel.tools"));
		
		selection = new SelectionWidget<>(width, 48, 48, null, (tool) -> EditorState.tool(tool));
		selection.setPadding(2);
		selection.setGap(2);
		VoxEdit.TOOL_REGISTRY.forEach(tool -> selection.addOption(tool, tool.asText()));
		addContent(selection);
	}
	

	@Override
	public void refreshPositions() {
		selection.setWidth(width);
		super.refreshPositions();
	}
}
