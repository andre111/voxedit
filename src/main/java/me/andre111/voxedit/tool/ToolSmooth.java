/*
 * Copyright (c) 2023 André Schweiger
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

import me.andre111.voxedit.editor.UndoRecordingStructureWorldAccess;
import me.andre111.voxedit.tool.config.ToolConfigSmooth;
import me.andre111.voxedit.tool.data.ToolTargeting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class ToolSmooth extends Tool<ToolConfigSmooth, ToolSmooth> {
	public ToolSmooth() {
		super(ToolConfigSmooth.CODEC, new ToolConfigSmooth());
	}

	@Override
	public void rightClick(UndoRecordingStructureWorldAccess world, PlayerEntity player, BlockHitResult target, ToolConfigSmooth config, Set<BlockPos> positions) {
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
	public void leftClick(UndoRecordingStructureWorldAccess world, PlayerEntity player, BlockHitResult target, ToolConfigSmooth config, Set<BlockPos> positions) {
	}

	@Override
	public Set<BlockPos> getBlockPositions(BlockView world, BlockHitResult target, ToolConfigSmooth config) {
		return ToolTargeting.getBlockPositions(world, target, config.radius(), config.shape(), null, config.filter());
	}
}
