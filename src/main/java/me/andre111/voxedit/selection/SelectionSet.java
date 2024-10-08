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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.mojang.serialization.Codec;

import me.andre111.voxedit.VoxEdit;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class SelectionSet implements Selection {
	public static final Codec<SelectionSet> CODEC = BlockPos.CODEC.listOf().xmap(list -> new SelectionSet(new HashSet<>(list)), sel -> new ArrayList<>(sel.set));
	
	private final Set<BlockPos> set;
	private final BlockBox boundingBox;
	
	public SelectionSet(Set<BlockPos> set) {
		this.set = Set.copyOf(set);
		this.boundingBox = BlockBox.encompassPositions(set).orElseGet(() -> new BlockBox(BlockPos.ORIGIN));
	}

	@Override
	public boolean contains(BlockPos pos) {
		return boundingBox.contains(pos) && set.contains(pos);
	}

	@Override
	public BlockBox getBoundingBox() {
		return boundingBox;
	}

	@Override
	public Iterator<BlockPos> iterator(Order order) {
		List<BlockPos> list = new ArrayList<>(set);
		list.sort(order.comparator);
		return list.iterator();
	}
	
	public SelectionSet withEnclosed() {
		Set<BlockPos> newSet = new HashSet<>(set);
		
		// do flood fill
		int value = 1;
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		int[][][] ff = new int[boundingBox.getBlockCountX()][boundingBox.getBlockCountY()][boundingBox.getBlockCountZ()];
		Set<Integer> outside = new HashSet<>();
		for(int x=0; x<boundingBox.getBlockCountX(); x++) {
			for(int y=0; y<boundingBox.getBlockCountY(); y++) {
				for(int z=0; z<boundingBox.getBlockCountZ(); z++) {
					if(ff[x][y][z] != 0) continue;
					mutable.set(boundingBox.getMinX()+x, boundingBox.getMinY()+y, boundingBox.getMinZ()+z);
					if(set.contains(mutable)) continue;
					
					doFloodFill(mutable, ff, outside, value++);
				}
			}
		}
		
		// transfer all enclosed areas
		for(int x=0; x<boundingBox.getBlockCountX(); x++) {
			for(int y=0; y<boundingBox.getBlockCountY(); y++) {
				for(int z=0; z<boundingBox.getBlockCountZ(); z++) {
					if(!outside.contains(ff[x][y][z])) newSet.add(new BlockPos(boundingBox.getMinX()+x, boundingBox.getMinY()+y, boundingBox.getMinZ()+z));
				}
			}
		}
		
		return new SelectionSet(newSet);
	}
	
	private void doFloodFill(BlockPos pos, int[][][] ff, Set<Integer> outside, int value) {
		if(!boundingBox.contains(pos)) return;
		
		Queue<BlockPos> toCheck = new LinkedList<>();
		toCheck.add(pos);
		
		while(!toCheck.isEmpty()) {
			pos = toCheck.poll();
			if(!boundingBox.contains(pos)) continue;
			
			int x = pos.getX()-boundingBox.getMinX();
			int y = pos.getY()-boundingBox.getMinY();
			int z = pos.getZ()-boundingBox.getMinZ();
			
			if(ff[x][y][z] != 0) return;
			if(set.contains(pos)) return;
			
			if(x == 0 || x == boundingBox.getBlockCountX()-1) outside.add(value);
			if(y == 0 || y == boundingBox.getBlockCountY()-1) outside.add(value);
			if(z == 0 || z == boundingBox.getBlockCountZ()-1) outside.add(value);
			
			ff[x][y][z] = value;
			
			for(Direction dir : Direction.values()) {
				BlockPos nb = pos.offset(dir);
				if(!toCheck.contains(nb)) toCheck.add(nb);
			}
		}
	}

	@Override
	public SelectionType<?> type() {
		return VoxEdit.SEL_SET;
	}
}
