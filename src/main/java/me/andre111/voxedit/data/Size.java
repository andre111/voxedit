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
package me.andre111.voxedit.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Size(boolean enabled, boolean split, int x, int y, int z) {
	public static final Codec<Size> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
				Codec.BOOL.fieldOf("enabled").forGetter(Size::enabled),
				Codec.BOOL.fieldOf("split").forGetter(Size::split),
				Codec.INT.fieldOf("x").forGetter(Size::x),
				Codec.INT.fieldOf("y").forGetter(Size::y),
				Codec.INT.fieldOf("z").forGetter(Size::z)
			)
			.apply(instance, Size::new));
	
	public Size enabled(boolean enabled) {
		return new Size(enabled, split, x, y, z);
	}
	public Size split(boolean split) {
		return new Size(enabled, split, x, y, z);
	}
	public Size size(int size) {
		return new Size(enabled, false, size, size, size);
	}
	public Size x(int x) {
		return new Size(enabled, split, x, y, z);
	}
	public Size y(int y) {
		return new Size(enabled, split, x, y, z);
	}
	public Size z(int z) {
		return new Size(enabled, split, x, y, z);
	}
}
