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
package me.andre111.voxedit.client.gui.widget.editor;

import java.util.List;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.client.EditorState;
import me.andre111.voxedit.client.gui.widget.SettingWidget;
import net.minecraft.text.Text;

public class EditorPanelFilter extends EditorPanel {
	//private List<SettingWidget<?, ?>> filterSettingWidgets = new ArrayList<>();
	private boolean rebuilding = false;

	public EditorPanelFilter(EditorWidget parent) {
		super(parent, VoxEdit.id("filter"), Text.translatable("voxedit.screen.panel.filter"));
		
		EditorState.CHANGE_FILTER.register((filter) -> rebuild());
	}

	private void rebuild() {
		if(rebuilding) return;
		
		rebuilding = true;
		clearContent();
		
		// settings
		var filter = EditorState.filter();
		if(filter != null) {
			List<SettingWidget<?, ?>> widgets = SettingWidget.forInstance(this, 0, 0, width, SettingWidget.BASE_HEIGTH, filter.value(), () -> EditorState.filter().config(), (config) -> {
				EditorState.persistant().filter(filter.with(config));
				parent.refreshPositions();
			}, (setting) -> {});
			addContent(widgets);
			for(var widget : widgets) widget.reload();
		}
		
		parent.refreshPositions();
		rebuilding = false;
	}
}
