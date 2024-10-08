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

import me.andre111.voxedit.data.Context;
import me.andre111.voxedit.data.Setting;
import me.andre111.voxedit.data.Target;
import me.andre111.voxedit.data.Config;
import me.andre111.voxedit.data.CommonToolSettings;
import me.andre111.voxedit.editor.EditorWorld;
import me.andre111.voxedit.filter.FilterContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class ToolFill extends VoxelTool {
	private static final Setting<Integer> RADIUS = Setting.ofInt("radius", 8, 1, 16);
	
	public ToolFill() {
		super(Properties.of(RADIUS, CommonToolSettings.TARGET_FLUIDS));
	}

	@Override
	public void place(EditorWorld world, PlayerEntity player, Target target, Context context, Config config, Set<BlockPos> positions) {
		for(BlockPos pos : positions) {
			world.setBlockState(pos, context.palette().getRandom(world.getRandom()), 0);
		}
	}

	@Override
	public void remove(EditorWorld world, PlayerEntity player, Target target, Context context, Config config, Set<BlockPos> positions) {
		for(BlockPos pos : positions) {
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 0);
		}
	}

	@Override
	public Set<BlockPos> getBlockPositions(BlockView world, Target target, Context context, Config config) {
		Set<BlockPos> positions = new HashSet<>();
		
		BlockPos center = target.getBlockPos();
		if(!shouldFill(world, center, world.getBlockState(center), null, context)) return positions;
		
		Block targetBlock = world.getBlockState(center).getBlock();	
		Queue<BlockPos> checkNeighbors = new LinkedList<>();
		
		positions.add(center);
		checkNeighbors.add(center);
		
		int radius = RADIUS.get(config);
		while(!checkNeighbors.isEmpty()) {
			BlockPos pos = checkNeighbors.poll();
			for(Direction dir : Direction.values()) {
				BlockPos neighbor = pos.offset(dir);
				if(Math.abs(neighbor.getX() - center.getX()) > radius) continue;
				if(Math.abs(neighbor.getY() - center.getY()) > radius) continue;
				if(Math.abs(neighbor.getZ() - center.getZ()) > radius) continue;
				if(!shouldFill(world, neighbor, world.getBlockState(neighbor), targetBlock, context)) continue;
				if(positions.contains(neighbor)) continue;
				if(checkNeighbors.contains(neighbor)) continue;
				
				positions.add(neighbor);
				checkNeighbors.add(neighbor);
			}
		}
		
		return positions;
	}
	
	private boolean shouldFill(BlockView view, BlockPos pos, BlockState blockState, Block targetBlock, Context context) {
		if(context.filter() != null && !context.filter().value().check(new FilterContext(view, pos), context.filter().config())) return false;
		if(targetBlock == null) return !blockState.isAir();
		else return blockState.getBlock() == targetBlock;
	}
}
