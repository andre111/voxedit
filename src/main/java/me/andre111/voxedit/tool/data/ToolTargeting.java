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
package me.andre111.voxedit.tool.data;

import java.util.HashSet;
import java.util.Set;

import me.andre111.voxedit.tool.shape.ConfiguredShape;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.BlockView;

public class ToolTargeting {
	public static Set<BlockPos> getBlockPositions(BlockView world, Target target) {
		return getBlockPositions(world, target, null, null, null);
	}
	public static Set<BlockPos> getBlockPositions(BlockView world, Target target, ConfiguredShape shape) {
		return getBlockPositions(world, target, shape, null, null);
	}
	public static Set<BlockPos> getBlockPositions(BlockView world, Target target, ConfiguredShape shape, TestPredicate testPredicate) {
		return getBlockPositions(world, target, shape, testPredicate, null);
	}
	public static Set<BlockPos> getBlockPositions(BlockView world, Target target, ConfiguredShape shape, TestPredicate testPredicate, BlockPalette filter) {
		Set<BlockPos> positions = new HashSet<>();
		
		// size
		int radiusX = shape.width();
		int radiusY = shape.width();
		int radiusZ = shape.width();
		if(shape.splitSize()) {
			Axis axis = target.side().get().getAxis();
			if(axis == Axis.X) {
				radiusX = shape.height();
				radiusY = shape.length();
				radiusZ = shape.width();
			} else if(axis == Axis.Y) {
				radiusX = shape.width();
				radiusY = shape.height();
				radiusZ = shape.length();
			} else if(axis == Axis.Z) {
				radiusX = shape.width();
				radiusY = shape.length();
				radiusZ = shape.height();
			}
		}
		
		// center + offset
		BlockPos center = target.getBlockPos();
		if(shape.offset()) {
			switch(target.side().get()) {
			case UP:
				center = center.add(shape.offsetW(), shape.offsetH(), shape.offsetL());
				break;
			case DOWN:
				center = center.add(shape.offsetW(), -shape.offsetH(), shape.offsetL());
				break;
			case NORTH:
				center = center.add(shape.offsetW(), shape.offsetL(), -shape.offsetH());
				break;
			case EAST:
				center = center.add(shape.offsetH(), shape.offsetL(), shape.offsetW());
				break;
			case SOUTH:
				center = center.add(shape.offsetW(), shape.offsetL(), shape.offsetH());
				break;
			case WEST:
				center = center.add(-shape.offsetH(), shape.offsetL(), shape.offsetW());
				break;
			}
		}
		
		// iterate box and do shape checks
		BlockPos.Mutable pos = new BlockPos.Mutable();
		for(int x = -radiusX; x <= radiusX; x++) {
        	for(int y = -radiusY; y <= radiusY; y++) {
        		for(int z = -radiusZ; z <= radiusZ; z++) {
                	if(shape != null && !shape.shape().contains(x, y, z, target.getSide(), radiusX, radiusY, radiusZ)) continue;
                	
                	pos.set(center.getX()+x, center.getY()+y, center.getZ()+z);
                	
                	if(testPredicate != null && !testPredicate.test(target, world, pos)) continue;
                	if(filter != null && filter.size() > 0 && !filter.has(world.getBlockState(pos).getBlock())) continue;
                	
        			positions.add(pos.toImmutable());
                }
            }
        }
		
		return positions;
	}
	
	public static boolean isFree(BlockView world, BlockPos pos) {
		return world.getBlockState(pos).getCollisionShape(world, pos).isEmpty();
	}
	
	public static boolean isSolid(BlockView world, BlockPos pos) {
		return world.getBlockState(pos).isFullCube(world, pos);
	}
	
	@FunctionalInterface
	public static interface TestPredicate {
		public boolean test(Target target, BlockView world, BlockPos pos);
	}
}
