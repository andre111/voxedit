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
import me.andre111.voxedit.tool.data.Mode;
import me.andre111.voxedit.tool.data.Shape;
import net.minecraft.text.Text;

public record ToolConfigBrush(BlockPalette palette, BlockPalette filter, Mode mode, Shape shape, int radius, boolean checkCanPlace, boolean targetFluids) implements ToolConfig {
	public static final Codec<ToolConfigBrush> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
					BlockPalette.CODEC.optionalFieldOf("palette", BlockPalette.DEFAULT).forGetter(ts -> ts.palette),
					BlockPalette.CODEC.optionalFieldOf("filter", new BlockPalette()).forGetter(ts -> ts.filter),
					Codec.STRING.optionalFieldOf("mode", Mode.SOLID.name()).xmap(str -> Mode.valueOf(str), mode -> mode.name()).forGetter(ts -> ts.mode),
					Codec.STRING.optionalFieldOf("shape", Shape.SPHERE.name()).xmap(str -> Shape.valueOf(str), shape -> shape.name()).forGetter(ts -> ts.shape),
					Codec.INT.optionalFieldOf("radius", 4).forGetter(ts -> ts.radius),
					Codec.BOOL.optionalFieldOf("checkCanPlace", false).forGetter(ts -> ts.checkCanPlace),
					Codec.BOOL.optionalFieldOf("targetFluids", false).forGetter(ts -> ts.targetFluids)
					)
			.apply(instance, ToolConfigBrush::new));

	@Override
	public  List<Text> getIconTexts() {
		return List.of(mode.asText(), shape.asText(), Text.of(radius+""));
	}
	
	@Override
	public BlockPalette getIconBlocks() {
		return palette;
	}

	public ToolConfigBrush() {
		this(BlockPalette.DEFAULT, new BlockPalette(), Mode.SOLID, Shape.SPHERE, 4, false, false);
	}

	public ToolConfigBrush withPalette(BlockPalette palette) {
		return new ToolConfigBrush(palette, filter, mode, shape, radius, checkCanPlace, targetFluids);
	}

	public ToolConfigBrush withFilter(BlockPalette filter) {
		return new ToolConfigBrush(palette, filter, mode, shape, radius, checkCanPlace, targetFluids);
	}

	public ToolConfigBrush withMode(Mode mode) {
		return new ToolConfigBrush(palette, filter, mode, shape, radius, checkCanPlace, targetFluids);
	}

	public ToolConfigBrush withShape(Shape shape) {
		return new ToolConfigBrush(palette, filter, mode, shape, radius, checkCanPlace, targetFluids);
	}

	public ToolConfigBrush withRadius(int radius) {
		return new ToolConfigBrush(palette, filter, mode, shape, radius, checkCanPlace, targetFluids);
	}

	public ToolConfigBrush withCheckCanPlace(boolean checkCanPlace) {
		return new ToolConfigBrush(palette, filter, mode, shape, radius, checkCanPlace, targetFluids);
	}

	public ToolConfigBrush withTargetFluids(boolean targetFluids) {
		return new ToolConfigBrush(palette, filter, mode, shape, radius, checkCanPlace, targetFluids);
	}
}
