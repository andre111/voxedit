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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.andre111.voxedit.tool.data.BlockPalette;
import me.andre111.voxedit.tool.data.ToolSettings;
import net.minecraft.text.Text;

public record ToolConfigFill(BlockPalette palette, BlockPalette filter, int radius, boolean targetFluids) implements ToolConfig<ToolConfigFill> {
	public static final Codec<ToolConfigFill> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
					BlockPalette.CODEC.optionalFieldOf("palette", BlockPalette.DEFAULT).forGetter(ts -> ts.palette),
					BlockPalette.CODEC.optionalFieldOf("filter", new BlockPalette()).forGetter(ts -> ts.filter),
					Codec.INT.optionalFieldOf("radius", 10).forGetter(ts -> ts.radius),
					Codec.BOOL.optionalFieldOf("targetFluids", false).forGetter(ts -> ts.targetFluids)
					)
			.apply(instance, ToolConfigFill::new));
	private static final ToolSettings<ToolConfigFill> SETTINGS = ToolSettings.create(instance -> instance
			.blockPalette(Text.of("Edit Palette"), true, true, ToolConfigFill::palette, ToolConfigFill::withPalette)
			.blockPalette(Text.of("Edit Filter"),  false, false, ToolConfigFill::filter, ToolConfigFill::withFilter)
			.integer(Text.of("Radius"), 1, 16, ToolConfigFill::radius, ToolConfigFill::withRadius));

	@Override
	public ToolSettings<ToolConfigFill> settings() {
		return SETTINGS;
	}

	@Override
	public  List<Text> getIconTexts() {
		return List.of(Text.of(radius+""));
	}
	
	@Override
	public BlockPalette getIconBlocks() {
		return palette;
	}
	
	public ToolConfigFill() {
		this(BlockPalette.DEFAULT, new BlockPalette(), 10, false);
	}

	public ToolConfigFill withPalette(BlockPalette palette) {
		return new ToolConfigFill(palette, filter, radius, targetFluids);
	}

	public ToolConfigFill withFilter(BlockPalette filter) {
		return new ToolConfigFill(palette, filter, radius, targetFluids);
	}

	public ToolConfigFill withRadius(int radius) {
		return new ToolConfigFill(palette, filter, radius, targetFluids);
	}

	public ToolConfigFill withTargetFluids(boolean targetFluids) {
		return new ToolConfigFill(palette, filter, radius, targetFluids);
	}
}
