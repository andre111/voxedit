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
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.andre111.voxedit.VoxEdit;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

public class SelectionSubtract implements Selection {
	public static final Codec<SelectionSubtract> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
				Selection.CODEC.fieldOf("first").forGetter(sel -> sel.first),
				Selection.CODEC.fieldOf("second").forGetter(sel -> sel.second)
			)
			.apply(instance, SelectionSubtract::new));
	
	private final Selection first;
	private final Selection second;
	private final BlockBox boundingBox;
	
	public SelectionSubtract(Selection first, Selection second) {
		this.first = first;
		this.second = second;
		this.boundingBox = first.getBoundingBox();
	}

	@Override
	public boolean contains(BlockPos pos) {
		if(!boundingBox.contains(pos)) return false;
		return first.contains(pos) && !second.contains(pos);
	}

	@Override
	public BlockBox getBoundingBox() {
		return boundingBox;
	}

	@Override
	public Iterator<BlockPos> iterator(Order order) {
		return new Iterator<>() {
			private final Iterator<BlockPos> firstIterator = first.iterator(order);
			
			private BlockPos next;
			
			{
				findNext();
			}

			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public BlockPos next() {
				BlockPos current = next;
				findNext();
				return current;
			}
			
			private void findNext() {
				next = null;
				while((next == null || second.contains(next)) && firstIterator.hasNext()) next = firstIterator.next();
				if(next != null && second.contains(next)) next = null;
			}
		};
	}

	@Override
	public SelectionType<?> type() {
		return VoxEdit.SEL_SUBTRACT;
	}
}
