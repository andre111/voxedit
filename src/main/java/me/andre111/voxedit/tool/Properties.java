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
package me.andre111.voxedit.tool;

import java.util.List;

import me.andre111.voxedit.tool.data.ToolSetting;

public record Properties(List<ToolSetting<?>> settings, boolean draggable, boolean showPreview, boolean noPresets) {
	public static final Properties NONE = of().create();
	
	public static Builder of(ToolSetting<?>... settings) {
		return new Builder(List.of(settings));
	}
	
	public static class Builder {
		private List<ToolSetting<?>> settings;
		private boolean draggable = false;
		private boolean showPreview = false;
		private boolean noPresets = false;
		
		private Builder(List<ToolSetting<?>> settings) {
			this.settings = settings;
		}
		
		public Builder draggable() {
			draggable = true;
			return this;
		}
		
		public Builder showPreview() {
			showPreview = true;
			return this;
		}
		
		public Builder noPresets() {
			noPresets = true;
			return this;
		}
		
		public Properties create() {
			return new Properties(List.copyOf(settings), draggable, showPreview, noPresets);
		}
	}
}
