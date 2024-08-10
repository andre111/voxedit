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
import net.minecraft.text.Text;

public class FilterBoolean extends Filter {
	public static final Setting<Op> OPERATION = Setting.ofEnum("operation", Op.class, Op::asText, true);
	public static final Setting<List<Configured<Filter>>> FILTERS = Setting.ofNested("filters", VoxEdit.TYPE_FILTER, new Configured<>(VoxEdit.FILTER_HEIGHT, VoxEdit.FILTER_HEIGHT.getDefaultConfig()), () -> VoxEdit.FILTER_REGISTRY.stream().toList(), true).listOf(List.of(), -1, Text.translatable("voxedit.filters"));
	
	public FilterBoolean() {
		super(List.of(OPERATION, FILTERS));
	}
	
	@Override
	public boolean check(FilterContext context, Config config) {
		switch(OPERATION.get(config)) {
		case AND:
			return FILTERS.get(config).stream().allMatch(f -> f.value().check(context, f.config()));
		case OR:
			return FILTERS.get(config).stream().anyMatch(f -> f.value().check(context, f.config()));
		default:
			throw new RuntimeException("Invalid boolean filter operation");
		}
	}
	
	public static enum Op {
		AND,
		OR;
		
		public Text asText() {
			return Text.translatable("voxedit.filter.boolean.operation."+name().toLowerCase());
		}
	}
}
