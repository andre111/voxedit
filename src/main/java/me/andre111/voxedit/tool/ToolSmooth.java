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

import java.util.List;
import java.util.Set;

import com.mojang.datafixers.util.Pair;

import me.andre111.voxedit.data.Context;
import me.andre111.voxedit.data.Target;
import me.andre111.voxedit.data.Config;
import me.andre111.voxedit.data.CommonToolSettings;
import me.andre111.voxedit.data.ToolTargeting;
import me.andre111.voxedit.editor.EditorWorld;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class ToolSmooth extends VoxelTool {
	public ToolSmooth() {
		super(Properties.of(CommonToolSettings.SHAPE, CommonToolSettings.TARGET_FLUIDS).draggable());
	}

	@Override
	public void place(EditorWorld world, PlayerEntity player, Target target, Context context, Config config, Set<BlockPos> positions) {
		// erode
		List<BlockPos> erodePositions = positions.stream().filter(pos -> !world.isAir(pos)).filter(pos -> {
			int neighborCount = (int) Direction.stream().map(pos::offset).filter(neighbor -> !world.isAir(neighbor)).count();
			return neighborCount < 5;
		}).toList();
		
		for(BlockPos pos : erodePositions) {
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 0);
		}
		
		// fill
		List<Pair<BlockPos, List<BlockPos>>> fillPositions = positions.stream().filter(pos -> world.isAir(pos)).map(pos -> {
			List<BlockPos> neighbors = Direction.stream().map(pos::offset).filter(neighbor -> !world.isAir(neighbor)).toList();
			return Pair.of(pos, neighbors);
		}).filter(pos -> pos.getSecond().size() > 2).toList();
		
		for(var pos : fillPositions) {
			world.setBlockState(pos.getFirst(), world.getBlockState(pos.getSecond().get(world.getRandom().nextInt(pos.getSecond().size()))), 0);
		}
	}

	@Override
	public void remove(EditorWorld world, PlayerEntity player, Target target, Context context, Config config, Set<BlockPos> positions) {
	}

	@Override
	public Set<BlockPos> getBlockPositions(BlockView world, Target target, Context context, Config config) {
		return ToolTargeting.getBlockPositions(world, target, CommonToolSettings.SHAPE.get(config), null, context.filter());
	}
}
