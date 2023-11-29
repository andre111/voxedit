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
import net.minecraft.text.Text;

public record ToolConfigSmooth(BlockPalette filter, Shape shape, int radius, boolean targetFluids) implements ToolConfig {
	public static final Codec<ToolConfigSmooth> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
					BlockPalette.CODEC.optionalFieldOf("filter", new BlockPalette()).forGetter(ts -> ts.filter),
					Codec.STRING.optionalFieldOf("shape", Shape.SPHERE.name()).xmap(str -> Shape.valueOf(str), shape -> shape.name()).forGetter(ts -> ts.shape),
					Codec.INT.optionalFieldOf("radius", 4).forGetter(ts -> ts.radius),
					Codec.BOOL.optionalFieldOf("targetFluids", false).forGetter(ts -> ts.targetFluids)
					)
			.apply(instance, ToolConfigSmooth::new));

	@Override
	public  List<Text> getIconTexts() {
		return List.of(shape.asText(), Text.of(radius+""));
	}
	
	public ToolConfigSmooth() {
		this(new BlockPalette(), Shape.SPHERE, 4, false);
	}

	public ToolConfigSmooth withFilter(BlockPalette filter) {
		return new ToolConfigSmooth(filter, shape, radius, targetFluids);
	}

	public ToolConfigSmooth withShape(Shape shape) {
		return new ToolConfigSmooth(filter, shape, radius, targetFluids);
	}

	public ToolConfigSmooth withRadius(int radius) {
		return new ToolConfigSmooth(filter, shape, radius, targetFluids);
	}

	public ToolConfigSmooth withTargetFluids(boolean targetFluids) {
		return new ToolConfigSmooth(filter, shape, radius, targetFluids);
	}
}
