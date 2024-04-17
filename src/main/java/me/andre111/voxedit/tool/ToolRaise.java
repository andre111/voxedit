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
package me.andre111.voxedit.tool;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

import me.andre111.voxedit.editor.EditHelper;
import me.andre111.voxedit.editor.EditorWorld;
import me.andre111.voxedit.selection.Selection;
import me.andre111.voxedit.selection.SelectionSet;
import me.andre111.voxedit.selection.SelectionSubtract;
import me.andre111.voxedit.tool.data.Context;
import me.andre111.voxedit.tool.data.Target;
import me.andre111.voxedit.tool.data.ToolConfig;
import me.andre111.voxedit.tool.data.ToolSetting;
import me.andre111.voxedit.tool.data.ToolTargeting;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class ToolRaise extends VoxelTool {
	private static final ToolSetting<Integer> RADIUS = ToolSetting.ofInt("radius", 8, 1, 32);
	private static final ToolSetting<Integer> FALLOFF = ToolSetting.ofInt("falloff", 4, 0, 16);
	private static final ToolSetting<Boolean> ROUNDED = ToolSetting.ofBoolean("rounded", true);
	private static final ToolSetting<Boolean> MOVE_CONNECTED = ToolSetting.ofBoolean("moveConnected", true);
	private static final ToolSetting<Boolean> INCLUDE_ENCLOSED = ToolSetting.ofBoolean("includeEnclosed", true);
	
	public ToolRaise() {
		super(Properties.of(RADIUS, FALLOFF, ROUNDED, MOVE_CONNECTED, INCLUDE_ENCLOSED));
	}

	@Override
	public void place(EditorWorld world, PlayerEntity player, Target target, Context context, ToolConfig config, Set<BlockPos> positions) {
		BlockPos center = target.getBlockPos();
		Direction dir = target.getSide();
		
		if(MOVE_CONNECTED.get(config)) {
			// find connected structures
			int maxSize = 4096 * 4;
			Set<BlockPos> connected = new HashSet<>();
			for(BlockPos pos : positions) {
				insertConnected(world, pos.offset(dir), connected, positions, maxSize, null);
			}
			if(connected.size() >= maxSize) {
				connected.clear();
				Predicate<BlockPos> test = (pos) -> switch(dir) {
				case UP -> pos.getY() > center.getY();
				case DOWN -> pos.getY() < center.getY();
				case EAST -> pos.getX() > center.getX();
				case NORTH -> pos.getZ() < center.getZ();
				case SOUTH -> pos.getZ() > center.getZ();
				case WEST -> pos.getX() < center.getX();
				};
				for(BlockPos pos : positions) {
					insertConnected(world, pos.offset(dir), connected, positions, maxSize, test);
				}
			}
			if(connected.size() >= maxSize) {
				//TODO: notify to much connected
				return;
			}
			if(!connected.isEmpty()) {
				SelectionSet selectionSet = new SelectionSet(connected);
				if(INCLUDE_ENCLOSED.get(config)) selectionSet = selectionSet.withEnclosed();
				Selection selection = new SelectionSubtract(selectionSet, new SelectionSet(positions));
				
				// move connected
				EditHelper.move(world, selection, dir.getOffsetX(), dir.getOffsetY(), dir.getOffsetZ());
			}
		}
		
		// raise selected blocks
		for(BlockPos pos : positions) {
			world.setBlockState(pos.offset(dir), world.getBlockState(pos), 0);
		}
	}

	@Override
	public void remove(EditorWorld world, PlayerEntity player, Target target, Context context, ToolConfig config, Set<BlockPos> positions) {
	}

	@Override
	public Set<BlockPos> getBlockPositions(BlockView world, Target target, Context context, ToolConfig config) {
		Set<BlockPos> positions = new HashSet<>();
		
		BlockPos center = target.getBlockPos();
		Direction dir = target.getSide();
		positions.add(center);
		
		int radius = RADIUS.get(config);
		int falloff = FALLOFF.get(config);
		boolean rounded = ROUNDED.get(config);
		for(int i=-radius-falloff; i<=radius+falloff; i++) {
			for(int j=-radius-falloff; j<=radius+falloff; j++) {
				double distance = rounded ? Math.sqrt(i*i + j*j) : Math.max(Math.abs(i), Math.abs(j));
				if(distance > radius+falloff) continue;
				
				int offset = 0;
				if(distance > radius) offset = (int) Math.ceil(distance - radius);
				
				BlockPos pos = switch(dir.getAxis()) {
				case X -> center.add(-dir.getOffsetX()*offset, i, j);
				case Y -> center.add(i, -dir.getOffsetY()*offset, j);
				case Z -> center.add(i, j, -dir.getOffsetZ()*offset);
				};
				
				// skip "non-needed" falloff
				if(distance > radius) {
					boolean skip = false;
					for(int above=1; above<=offset; above++) {
						if(ToolTargeting.isSolid(world, pos.offset(dir, above))) skip = true;
					}
					if(skip) continue;
				}
				
				// find closest surface in range
				int steps = 0;
				while(!ToolTargeting.isSolid(world, pos) && steps < falloff-offset) {
					pos = pos.offset(dir.getOpposite());
					steps++;
				}
				
				// only accept solid surfaces with non-solid infront
				if(!ToolTargeting.isSolid(world, pos)) continue;
				//if(ToolTargeting.isSolid(world, pos.offset(dir))) continue;
				
				positions.add(pos);
			}
		}
		
		return positions;
	}

	private void insertConnected(BlockView world, BlockPos pos, Set<BlockPos> connected, Set<BlockPos> excluded, int maxSize, Predicate<BlockPos> test) {
		if(world.getBlockState(pos).isAir()) return;
		
		Queue<BlockPos> toCheck = new LinkedList<>();
		toCheck.add(pos);
		
		while(!toCheck.isEmpty()) {
			pos = toCheck.poll();
			BlockState state = world.getBlockState(pos);
			VoxelShape shape = state.getOutlineShape(world, pos);
			if(shape.isEmpty()) continue;
			
			connected.add(pos);
			if(connected.size() >= maxSize) continue;
			
			Box box = shape.getBoundingBox();
			for(Direction dir : Direction.values()) {
				if(dir.getDirection() == AxisDirection.POSITIVE) {
					if(box.getMax(dir.getAxis()) < 1) continue;
				} else {
					if(box.getMin(dir.getAxis()) > 0) continue;
				}
				
				BlockPos nb = pos.offset(dir);

				if(test != null && !test.test(nb)) continue;
				if(world.isOutOfHeightLimit(nb)) continue;
				if(world.getBlockState(nb).isAir()) continue;
				if(excluded.contains(nb)) continue;
				if(connected.contains(nb)) continue;
				if(toCheck.contains(nb)) continue;
				
				BlockState nbState = world.getBlockState(nb);
				VoxelShape nbShape = nbState.getOutlineShape(world, nb);
				if(nbShape.isEmpty()) continue;
				Box nbBox = nbShape.getBoundingBox();
				if(dir.getDirection() == AxisDirection.POSITIVE) {
					if(nbBox.getMin(dir.getAxis()) > 0) continue;
				} else {
					if(nbBox.getMax(dir.getAxis()) < 1) continue;
				}
				
				toCheck.add(nb);
			}
		}
	}
}
