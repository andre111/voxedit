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
package me.andre111.voxedit.filter;

import java.util.List;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.data.Config;
import me.andre111.voxedit.data.Configured;
import me.andre111.voxedit.data.Setting;

public class FilterOffset extends Filter {
	public static final Setting<Configured<Filter>> FILTER = Setting.ofNested("filter", VoxEdit.TYPE_FILTER, new Configured<>(VoxEdit.FILTER_HEIGHT, VoxEdit.FILTER_HEIGHT.getDefaultConfig()), () -> VoxEdit.FILTER_REGISTRY.stream().toList(), true);
	private static final Setting<Integer> OFFSET_X = Setting.ofInt("offsetX", 0, -16, 16);
	private static final Setting<Integer> OFFSET_Y = Setting.ofInt("offsetY", 0, -16, 16);
	private static final Setting<Integer> OFFSET_Z = Setting.ofInt("offsetZ", 0, -16, 16);
	
	public FilterOffset() {
		super(List.of(OFFSET_X, OFFSET_Y, OFFSET_Z, FILTER));
	}
	
	@Override
	public boolean check(FilterContext context, Config config) {
		var filter = FILTER.get(config);
		return filter != null && filter.value().check(context.at(context.pos().add(OFFSET_X.get(config), OFFSET_Y.get(config), OFFSET_Z.get(config))), filter.config());
	}
}
