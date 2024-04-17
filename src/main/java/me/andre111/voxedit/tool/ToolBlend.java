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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.andre111.voxedit.editor.EditorWorld;
import me.andre111.voxedit.tool.data.Context;
import me.andre111.voxedit.tool.data.Target;
import me.andre111.voxedit.tool.data.ToolConfig;
import me.andre111.voxedit.tool.data.ToolSetting;
import me.andre111.voxedit.tool.data.ToolSettings;
import me.andre111.voxedit.tool.data.ToolTargeting;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class ToolBlend extends VoxelTool {
	private static final ToolSetting<Integer> RADIUS = ToolSetting.ofInt("radius", 5, 1, 16);
	
	public ToolBlend() {
		super(Properties.of(ToolSettings.SHAPE, RADIUS, ToolSettings.TARGET_FLUIDS).draggable());
	}

	@Override
	public void place(EditorWorld world, PlayerEntity player, Target target, Context context, ToolConfig config, Set<BlockPos> positions) {
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
	public void remove(EditorWorld world, PlayerEntity player, Target target, Context context, ToolConfig config, Set<BlockPos> positions) {
	}

	@Override
	public Set<BlockPos> getBlockPositions(BlockView world, Target target, Context context, ToolConfig config) {
		if(ToolTargeting.isFree(world, target.getBlockPos())) return Set.of();
		
		return ToolTargeting.getBlockPositions(world, target, RADIUS.get(config), ToolSettings.SHAPE.get(config), (hit, testWorld, testPos) -> !ToolTargeting.isFree(testWorld, testPos), context.filter());
	}
}
