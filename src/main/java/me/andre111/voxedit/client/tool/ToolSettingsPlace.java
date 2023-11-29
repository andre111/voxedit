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
import me.andre111.voxedit.tool.config.ToolConfigPlace;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

@Environment(value=EnvType.CLIENT)
public class ToolSettingsPlace implements ToolSettings {
	private static List<? extends ToolSettingWidget<?, ?>> SETTINGS = List.of(
			ToolSettingWidget.identifier(Text.of("Feature"), RegistryKeys.CONFIGURED_FEATURE, 
					ToolConfigPlace::feature, 
					ToolConfigPlace::withFeature),
			ToolSettingWidget.intRange(Text.of("Tries"), 1, 10,
					ToolConfigPlace::tries, 
					ToolConfigPlace::withTries)
			);

	@Override
	public List<? extends ToolSettingWidget<?, ?>> getSettings() {
		return SETTINGS;
	}

}
