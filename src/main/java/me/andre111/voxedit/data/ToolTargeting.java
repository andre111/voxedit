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

import java.util.HashSet;
import java.util.Set;

import me.andre111.voxedit.filter.Filter;
import me.andre111.voxedit.filter.FilterContext;
import me.andre111.voxedit.shape.Shape;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.BlockView;

public class ToolTargeting {
	public static Set<BlockPos> getBlockPositions(BlockView world, Target target) {
		return getBlockPositions(world, target, null, null, null);
	}
	public static Set<BlockPos> getBlockPositions(BlockView world, Target target, Configured<Shape> shape) {
		return getBlockPositions(world, target, shape, null, null);
	}
	public static Set<BlockPos> getBlockPositions(BlockView world, Target target, Configured<Shape> shape, TestPredicate testPredicate) {
		return getBlockPositions(world, target, shape, testPredicate, null);
	}
	public static Set<BlockPos> getBlockPositions(BlockView world, Target target, Configured<Shape> shape, TestPredicate testPredicate, Configured<Filter> filter) {
		Set<BlockPos> positions = new HashSet<>();
		
		// size
		Size radius = Shape.SIZE.get(shape.config());
		int radiusX = radius.x();
		int radiusY = radius.y();
		int radiusZ = radius.z();
		if(radius.split()) {
			Axis axis = target.side().get().getAxis();
			if(axis == Axis.X) {
				radiusX = radius.y();
				radiusY = radius.z();
				radiusZ = radius.x();
			} else if(axis == Axis.Y) {
				radiusX = radius.x();
				radiusY = radius.y();
				radiusZ = radius.z();
			} else if(axis == Axis.Z) {
				radiusX = radius.x();
				radiusY = radius.y();
				radiusZ = radius.z();
			}
		}
		
		// center + offset
		BlockPos center = target.getBlockPos();
		Size offset = Shape.OFFSET.get(shape.config());
		if(offset.enabled()) {
			switch(target.side().get()) {
			case UP:
				center = center.add(offset.x(), offset.y(), offset.z());
				break;
			case DOWN:
				center = center.add(offset.x(), -offset.y(), offset.z());
				break;
			case NORTH:
				center = center.add(offset.x(), offset.z(), -offset.y());
				break;
			case EAST:
				center = center.add(offset.y(), offset.z(), offset.x());
				break;
			case SOUTH:
				center = center.add(offset.x(), offset.z(), offset.y());
				break;
			case WEST:
				center = center.add(-offset.y(), offset.z(), offset.x());
				break;
			}
		}
		
		// iterate box and do shape checks
		BlockPos.Mutable pos = new BlockPos.Mutable();
		for(int x = -radiusX; x <= radiusX; x++) {
        	for(int y = -radiusY; y <= radiusY; y++) {
        		for(int z = -radiusZ; z <= radiusZ; z++) {
                	if(shape != null && !shape.value().contains(x, y, z, target.getSide(), radiusX, radiusY, radiusZ)) continue;
                	
                	pos.set(center.getX()+x, center.getY()+y, center.getZ()+z);
                	
                	if(testPredicate != null && !testPredicate.test(target, world, pos)) continue;
                	if(filter != null && filter.value() != null && !filter.value().check(new FilterContext(world, pos), filter.config())) continue;
                	
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
