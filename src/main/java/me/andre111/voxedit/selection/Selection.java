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

import java.util.Iterator;

import com.mojang.serialization.Codec;

import me.andre111.voxedit.VoxEdit;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

public interface Selection {
	public boolean contains(BlockPos pos);
	public BlockBox getBoundingBox();
	public Iterator<BlockPos> iterator(Order order);
	public SelectionType<?> type();
	
	public static final Codec<Selection> CODEC = VoxEdit.SELECTION_TYPE_REGISTRY.getCodec().dispatch("type", Selection::type, type -> type.getCodec().fieldOf("value"));
	
	public static Selection combine(Selection s1, Selection s2, SelectionMode mode) {
		switch(mode) {
		case ADD:
			return s1 == null ? s2 : new SelectionAdd(s1, s2);
		case SUBTRACT:
			return s1 == null ? null : new SelectionSubtract(s1, s2);
		case REPLACE:
			return s2;
		default:
			return s2;
		}
	}
}
