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
package me.andre111.voxedit.shape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.data.Configurable;
import me.andre111.voxedit.data.Setting;
import me.andre111.voxedit.data.Size;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public abstract class Shape implements Configurable<Shape> {
	public static final Setting<Size> SIZE = Setting.ofSize("size", new Size(true, false, 4, 4, 4), false, true, 1, 16);
	public static final Setting<Size> OFFSET = Setting.ofSize("offset", new Size(false, true, 0, 0, 0), true, false, -16, 16);
	
	private final List<Setting<?>> settings;
	
	public Shape() {
		this(List.of());
	}
	public Shape(List<Setting<?>> settings) {
		List<Setting<?>> allSettings = new ArrayList<>();
		allSettings.add(SIZE);
		allSettings.add(OFFSET);
		allSettings.addAll(settings);
		this.settings = Collections.unmodifiableList(allSettings);
	}

	@Override
	public List<Setting<?>> getSettings() {
		return settings;
	}

	@Override
	public Type<Shape> getType() {
		return VoxEdit.TYPE_SHAPE;
	}
	
	public Identifier id() {
		return VoxEdit.SHAPE_REGISTRY.getId(this);
	}
	
	@Override
	public Text getName() {
		return Text.translatable("voxedit.shape."+id().toTranslationKey());
	}
	
	public abstract boolean contains(int x, int y, int z, Direction direction, double sizeX, double sizeY, double sizeZ);
}
