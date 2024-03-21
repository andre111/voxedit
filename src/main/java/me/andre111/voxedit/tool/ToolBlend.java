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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.andre111.voxedit.editor.UndoRecordingStructureWorldAccess;
import me.andre111.voxedit.tool.config.ToolConfigBlend;
import me.andre111.voxedit.tool.data.ToolTargeting;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class ToolBlend extends Tool<ToolConfigBlend, ToolBlend> {
	public ToolBlend() {
		super(ToolConfigBlend.CODEC, new ToolConfigBlend());
	}

	@Override
	public void rightClick(UndoRecordingStructureWorldAccess world, PlayerEntity player, BlockHitResult target, ToolConfigBlend config, Set<BlockPos> positions) {
		List<BlockState> neighbors = new ArrayList<>();
		for(BlockPos pos : positions) {
			// find all solid neighbors
			neighbors.clear();
			for(Direction dir : Direction.values()) {
				BlockPos offset = pos.offset(dir);
				if(positions.contains(offset) && !ToolTargeting.isFree(world, offset)) neighbors.add(world.getBlockState(offset));
			}
			if(neighbors.isEmpty()) continue;
			
			// select random neighbor
			BlockState newBlockState = neighbors.get(world.getRandom().nextInt(neighbors.size()));
			world.setBlockState(pos, newBlockState, 0);
		}
	}

	@Override
	public void leftClick(UndoRecordingStructureWorldAccess world, PlayerEntity player, BlockHitResult target, ToolConfigBlend config, Set<BlockPos> positions) {
	}

	@Override
	public Set<BlockPos> getBlockPositions(BlockView world, BlockHitResult target, ToolConfigBlend config) {
		BlockPos center = target.getBlockPos();
		if(ToolTargeting.isFree(world, center)) return Set.of();
		
		return ToolTargeting.getBlockPositions(world, target, config.radius(), config.shape(), (hit, testWorld, testPos) -> !ToolTargeting.isFree(testWorld, testPos), config.filter());
	}
}
