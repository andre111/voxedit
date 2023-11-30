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
import me.andre111.voxedit.tool.data.Shape;
import me.andre111.voxedit.tool.data.ToolSettings;
import net.minecraft.text.Text;

public record ToolConfigFlatten(BlockPalette palette, Shape shape, int radius, boolean targetFluids) implements ToolConfig<ToolConfigFlatten> {
	public static final Codec<ToolConfigFlatten> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
					BlockPalette.CODEC.optionalFieldOf("palette", BlockPalette.DEFAULT).forGetter(ts -> ts.palette),
					Codec.STRING.optionalFieldOf("shape", Shape.SPHERE.name()).xmap(str -> Shape.valueOf(str), shape -> shape.name()).forGetter(ts -> ts.shape),
					Codec.INT.optionalFieldOf("radius", 5).forGetter(ts -> ts.radius),
					Codec.BOOL.optionalFieldOf("targetFluids", false).forGetter(ts -> ts.targetFluids)
					)
			.apply(instance, ToolConfigFlatten::new));
	private static final ToolSettings< ToolConfigFlatten> SETTINGS = ToolSettings.create(instance -> instance
			.blockPalette(Text.translatable("voxedit.tool.settings.palette"), true, true, ToolConfigFlatten::palette, ToolConfigFlatten::withPalette)
			.fixedValues(Text.translatable("voxedit.shape"), Shape.values(), Shape::asText, ToolConfigFlatten::shape, ToolConfigFlatten::withShape)
			.integer(Text.translatable("voxedit.tool.settings.radius"), 1, 16, ToolConfigFlatten::radius, ToolConfigFlatten::withRadius));

	@Override
	public ToolSettings<ToolConfigFlatten> settings() {
		return SETTINGS;
	}

	@Override
	public  List<Text> getIconTexts() {
		return List.of(shape.asText(), Text.of(radius+""));
	}
	
	@Override
	public BlockPalette getIconBlocks() {
		return palette;
	}
	
	public ToolConfigFlatten() {
		this(BlockPalette.DEFAULT, Shape.SPHERE, 5, false);
	}

	public ToolConfigFlatten withPalette(BlockPalette palette) {
		return new ToolConfigFlatten(palette, shape, radius, targetFluids);
	}

	public ToolConfigFlatten withShape(Shape shape) {
		return new ToolConfigFlatten(palette, shape, radius, targetFluids);
	}

	public ToolConfigFlatten withRadius(int radius) {
		return new ToolConfigFlatten(palette, shape, radius, targetFluids);
	}

	public ToolConfigFlatten withTargetFluids(boolean targetFluids) {
		return new ToolConfigFlatten(palette, shape, radius, targetFluids);
	}
}
