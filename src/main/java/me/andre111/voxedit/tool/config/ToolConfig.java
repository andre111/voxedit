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
package me.andre111.voxedit.tool.config;

import java.util.List;

import me.andre111.voxedit.tool.data.BlockPalette;
import me.andre111.voxedit.tool.data.ToolSettings;
import net.minecraft.text.Text;

public interface ToolConfig<TC extends ToolConfig<TC>> {
	public ToolSettings<TC> settings();
	
	public int radius();
	public ToolConfig<TC> withRadius(int radius);
	
	public default boolean targetFluids() {
		return false;
	}
	
	public default List<Text> getIconTexts() {
		return List.of();
	}
	
	public default BlockPalette getIconBlocks() {
		return null;
	}
}
