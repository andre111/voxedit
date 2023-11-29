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
package me.andre111.voxedit.client.tool;

import java.util.List;

import me.andre111.voxedit.client.gui.widget.ToolSettingWidget;
import me.andre111.voxedit.tool.config.ToolConfigBrush;
import me.andre111.voxedit.tool.data.Mode;
import me.andre111.voxedit.tool.data.Shape;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

@Environment(value=EnvType.CLIENT)
public class ToolSettingsBrush implements ToolSettings {
	private static List<? extends ToolSettingWidget<?, ?>> SETTINGS = List.of(
			ToolSettingWidget.blockPalette(Text.of("Edit Palette"), true, true,
					ToolConfigBrush::palette, 
					ToolConfigBrush::withPalette),
			ToolSettingWidget.blockPalette(Text.of("Edit Filter"),  false, false,
					ToolConfigBrush::filter, 
					ToolConfigBrush::withFilter),
			ToolSettingWidget.ofEnum(Text.of("Mode"), Mode::asText, Mode.values(), 
					ToolConfigBrush::mode, 
					ToolConfigBrush::withMode),
			ToolSettingWidget.ofEnum(Text.of("Shape"), Shape::asText, Shape.values(), 
					ToolConfigBrush::shape, 
					ToolConfigBrush::withShape),
			ToolSettingWidget.intRange(Text.of("Radius"), 1, 16,
					ToolConfigBrush::radius, 
					ToolConfigBrush::withRadius),
			ToolSettingWidget.bool(Text.of("Check valid"),
					ToolConfigBrush::checkCanPlace, 
					ToolConfigBrush::withCheckCanPlace)
			);

	@Override
	public List<? extends ToolSettingWidget<?, ?>> getSettings() {
		return SETTINGS;
	}
}
