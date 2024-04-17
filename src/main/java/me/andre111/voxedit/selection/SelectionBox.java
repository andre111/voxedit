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

import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

public class SelectionBox implements Selection {
	private final BlockBox box;
	
	public SelectionBox(BlockBox box) {
		this.box = box;
	}
	
	@Override
	public boolean contains(BlockPos pos) {
		return box.contains(pos);
	}

	@Override
	public BlockBox getBoundingBox() {
		return box;
	}

	@Override
	public Iterator<BlockPos> iterator(Order order) {
		return switch(order) {
		case X_MIN_TO_MAX -> new Iterator<>() {
			private BlockPos.Mutable next = new BlockPos.Mutable(box.getMinX(), box.getMinY(), box.getMinZ());
			private BlockPos.Mutable returnable = new BlockPos.Mutable();
			
			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public BlockPos next() {
				returnable.set(next);
				if(next.getY() < box.getMaxY()) next.set(next.getX(), next.getY() + 1, next.getZ());
				else if(next.getZ() < box.getMaxZ()) next.set(next.getX(), box.getMinY(), next.getZ() + 1);
				else if(next.getX() > box.getMinX()) next.set(next.getX()-1, box.getMinY(), box.getMinZ());
				else next = null;
				return returnable;
			}
		};
		case X_MAX_TO_MIN -> new Iterator<>() {
			private BlockPos.Mutable next = new BlockPos.Mutable(box.getMaxX(), box.getMinY(), box.getMinZ());
			private BlockPos.Mutable returnable = new BlockPos.Mutable();
			
			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public BlockPos next() {
				returnable.set(next);
				if(next.getY() < box.getMaxY()) next.set(next.getX(), next.getY() + 1, next.getZ());
				else if(next.getZ() < box.getMaxZ()) next.set(next.getX(), box.getMinY(), next.getZ() + 1);
				else if(next.getX() < box.getMaxX()) next.set(next.getX()+1, box.getMinY(), box.getMinZ());
				else next = null;
				return returnable;
			}
		};
		case Y_MIN_TO_MAX -> new Iterator<>() {
			private BlockPos.Mutable next = new BlockPos.Mutable(box.getMinX(), box.getMinY(), box.getMinZ());
			private BlockPos.Mutable returnable = new BlockPos.Mutable();
			
			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public BlockPos next() {
				returnable.set(next);
				if(next.getX() < box.getMaxX()) next.set(next.getX()+1, next.getY(), next.getZ());
				else if(next.getZ() < box.getMaxZ()) next.set(box.getMinX(), next.getY(), next.getZ() + 1);
				else if(next.getY() < box.getMaxY()) next.set(box.getMinX(), next.getY() + 1, box.getMinZ());
				else next = null;
				return returnable;
			}
		};
		case Y_MAX_TO_MIN -> new Iterator<>() {
			private BlockPos.Mutable next = new BlockPos.Mutable(box.getMinX(), box.getMaxY(), box.getMinZ());
			private BlockPos.Mutable returnable = new BlockPos.Mutable();
			
			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public BlockPos next() {
				returnable.set(next);
				if(next.getX() < box.getMaxX()) next.set(next.getX()+1, next.getY(), next.getZ());
				else if(next.getZ() < box.getMaxZ()) next.set(box.getMinX(), next.getY(), next.getZ() + 1);
				else if(next.getY() > box.getMinY()) next.set(box.getMinX(), next.getY() - 1, box.getMinZ());
				else next = null;
				return returnable;
			}
		};
		case Z_MIN_TO_MAX -> new Iterator<>() {
			private BlockPos.Mutable next = new BlockPos.Mutable(box.getMinX(), box.getMinY(), box.getMinZ());
			private BlockPos.Mutable returnable = new BlockPos.Mutable();
			
			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public BlockPos next() {
				returnable.set(next);
				if(next.getX() < box.getMaxX()) next.set(next.getX()+1, next.getY(), next.getZ());
				else if(next.getY() < box.getMaxY()) next.set(box.getMinX(), next.getY() + 1, next.getZ());
				else if(next.getZ() < box.getMaxZ()) next.set(box.getMinX(), box.getMinY(), next.getZ() + 1);
				else next = null;
				return returnable;
			}
		};
		case Z_MAX_TO_MIN -> new Iterator<>() {
			private BlockPos.Mutable next = new BlockPos.Mutable(box.getMinX(), box.getMinY(), box.getMaxZ());
			private BlockPos.Mutable returnable = new BlockPos.Mutable();
			
			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public BlockPos next() {
				returnable.set(next);
				if(next.getX() < box.getMaxX()) next.set(next.getX()+1, next.getY(), next.getZ());
				else if(next.getY() < box.getMaxY()) next.set(box.getMinX(), next.getY() + 1, next.getZ());
				else if(next.getZ() > box.getMinZ()) next.set(box.getMinX(), box.getMinY(), next.getZ() - 1);
				else next = null;
				return returnable;
			}
		};
		default -> throw new IllegalArgumentException("Unexpected value: " + order);
		};
	}
}
