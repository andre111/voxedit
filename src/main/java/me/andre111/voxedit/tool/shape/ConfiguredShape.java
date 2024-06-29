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
package me.andre111.voxedit.tool.shape;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.andre111.voxedit.VoxEdit;

//TODO: lock orientation toggle?
public record ConfiguredShape(Shape shape, boolean splitSize, int width, int height, int length, boolean offset, int offsetW, int offsetH, int offsetL) {
	public static final Codec<ConfiguredShape> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
				VoxEdit.SHAPE_REGISTRY.getCodec().fieldOf("shape").forGetter(ConfiguredShape::shape),
				Codec.BOOL.optionalFieldOf("splitSize", false).forGetter(ConfiguredShape::splitSize),
				Codec.INT.fieldOf("width").forGetter(ConfiguredShape::width),
				Codec.INT.fieldOf("height").forGetter(ConfiguredShape::height),
				Codec.INT.fieldOf("length").forGetter(ConfiguredShape::length),
				Codec.BOOL.optionalFieldOf("offset", false).forGetter(ConfiguredShape::offset),
				Codec.INT.optionalFieldOf("offsetX", 0).forGetter(ConfiguredShape::offsetW),
				Codec.INT.optionalFieldOf("offsetY", 0).forGetter(ConfiguredShape::offsetH),
				Codec.INT.optionalFieldOf("offsetZ", 0).forGetter(ConfiguredShape::offsetL)
			)
			.apply(instance, ConfiguredShape::new));
	
	public boolean isValid() {
		if(shape == null) return false;
		if(width < 1 || width > 16) return false;
		if(height < 1 || height > 16) return false;
		if(length < 1 || length > 16) return false;
		return true;
	}
	
	public ConfiguredShape shape(Shape shape) {
		return new ConfiguredShape(shape, splitSize, width, height, length, offset, offsetW, offsetH, offsetL);
	}
	
	public ConfiguredShape splitSize(boolean splitSize) {
		return new ConfiguredShape(shape, splitSize, width, height, length, offset, offsetW, offsetH, offsetL);
	}
	
	public ConfiguredShape size(int size) {
		return new ConfiguredShape(shape, false, size, size, size, offset, offsetW, offsetH, offsetL);
	}
	
	public ConfiguredShape width(int width) {
		return new ConfiguredShape(shape, splitSize, width, height, length, offset, offsetW, offsetH, offsetL);
	}
	
	public ConfiguredShape height(int height) {
		return new ConfiguredShape(shape, splitSize, width, height, length, offset, offsetW, offsetH, offsetL);
	}
	
	public ConfiguredShape length(int length) {
		return new ConfiguredShape(shape, splitSize, width, height, length, offset, offsetW, offsetH, offsetL);
	}
	
	public ConfiguredShape offset(boolean offset) {
		return new ConfiguredShape(shape, splitSize, width, height, length, offset, offsetW, offsetH, offsetL);
	}
	
	public ConfiguredShape offsetW(int offsetW) {
		return new ConfiguredShape(shape, splitSize, width, height, length, offset, offsetW, offsetH, offsetL);
	}
	
	public ConfiguredShape offsetH(int offsetH) {
		return new ConfiguredShape(shape, splitSize, width, height, length, offset, offsetW, offsetH, offsetL);
	}
	
	public ConfiguredShape offsetL(int offsetL) {
		return new ConfiguredShape(shape, splitSize, width, height, length, offset, offsetW, offsetH, offsetL);
	}
}
