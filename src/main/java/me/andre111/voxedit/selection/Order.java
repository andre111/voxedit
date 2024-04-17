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
package me.andre111.voxedit.selection;

import java.util.Comparator;

import net.minecraft.util.math.BlockPos;

public enum Order {
	X_MIN_TO_MAX((a, b) -> a.getX() - b.getX()),
	X_MAX_TO_MIN((a, b) -> b.getX() - a.getX()),
	Y_MIN_TO_MAX((a, b) -> a.getY() - b.getY()),
	Y_MAX_TO_MIN((a, b) -> b.getY() - a.getY()),
	Z_MIN_TO_MAX((a, b) -> a.getZ() - b.getZ()),
	Z_MAX_TO_MIN((a, b) -> b.getZ() - a.getZ());
	
	public final Comparator<BlockPos> comparator;
	
	Order(Comparator<BlockPos> comparator) {
		this.comparator = comparator;
	}
}
