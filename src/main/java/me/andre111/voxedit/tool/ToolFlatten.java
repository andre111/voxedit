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

import java.util.Set;

import me.andre111.voxedit.editor.UndoRecordingStructureWorldAccess;
import me.andre111.voxedit.tool.config.ToolConfigFlatten;
import me.andre111.voxedit.tool.data.ToolTargeting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class ToolFlatten extends Tool<ToolConfigFlatten, ToolFlatten> {
	public ToolFlatten() {
		super(ToolConfigFlatten.CODEC, new ToolConfigFlatten());
	}

	@Override
	public void rightClick(UndoRecordingStructureWorldAccess world, PlayerEntity player, BlockHitResult target, ToolConfigFlatten config, Set<BlockPos> positions) {
		for(BlockPos pos : positions) {
			if(ToolTargeting.isFree(world, pos)) {
				world.setBlockState(pos, config.palette().getRandom(world.getRandom()), 0);
			} else {
				world.setBlockState(pos, Blocks.AIR.getDefaultState(), 0);
			}
		}
	}

	@Override
	public void leftClick(UndoRecordingStructureWorldAccess world, PlayerEntity player, BlockHitResult target, ToolConfigFlatten config, Set<BlockPos> positions) {
	}

	@Override
	public Set<BlockPos> getBlockPositions(BlockView world, BlockHitResult target, ToolConfigFlatten config) {
		BlockPos center = target.getBlockPos();
		if(ToolTargeting.isFree(world, center)) return Set.of();
		
		Set<BlockPos> positions = ToolTargeting.getBlockPositions(world, target, config.radius(), config.shape());
		positions.removeIf(pos -> {
			int offset = switch(target.getSide()) {
			case UP -> pos.getY() - center.getY();
			case DOWN -> center.getY() - pos.getY();
			case SOUTH -> pos.getZ() - center.getZ();
			case NORTH -> center.getZ() - pos.getZ();
			case EAST -> pos.getX() - center.getX();
			case WEST -> center.getX() - pos.getX();
			};
			if(offset <= 0) return !ToolTargeting.isFree(world, pos);
			else return ToolTargeting.isFree(world, pos);
		});
		return positions;
	}
}
