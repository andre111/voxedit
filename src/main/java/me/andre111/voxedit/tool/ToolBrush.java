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

import me.andre111.voxedit.editor.EditorWorld;
import me.andre111.voxedit.tool.data.Context;
import me.andre111.voxedit.tool.data.Shape;
import me.andre111.voxedit.tool.data.Target;
import me.andre111.voxedit.tool.data.ToolConfig;
import me.andre111.voxedit.tool.data.ToolSetting;
import me.andre111.voxedit.tool.data.ToolSettings;
import me.andre111.voxedit.tool.data.ToolTargeting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class ToolBrush extends Tool {
	private static final ToolSetting<Shape> SHAPE = ToolSetting.ofEnum("shape", Shape.class, Shape::asText);
	private static final ToolSetting<Integer> RADIUS = ToolSetting.ofInt("radius", 5, 1, 16);
	private static final ToolSetting<Boolean> CHECK_CAN_PLACE = ToolSetting.ofBoolean("checkCanPlace", false);
	
	public ToolBrush() {
		super(Properties.of(SHAPE, RADIUS, CHECK_CAN_PLACE, ToolSettings.TARGET_FLUIDS).draggable());
	}

	@Override
	public void place(EditorWorld world, PlayerEntity player, Target target, Context context, ToolConfig config, Set<BlockPos> positions) {
		boolean checkCanPlace = CHECK_CAN_PLACE.get(config);
		for(BlockPos pos : positions) {
			BlockState state = context.palette().getRandom(world.getRandom());
			if(!checkCanPlace || state.canPlaceAt(world, pos)) world.setBlockState(pos, state, 0);
		}
	}

	@Override
	public void remove(EditorWorld world, PlayerEntity player, Target target, Context context, ToolConfig config, Set<BlockPos> positions) {
		for(BlockPos pos : positions) {
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 0);
		}
	}

	@Override
	public Set<BlockPos> getBlockPositions(BlockView world, Target target, Context context, ToolConfig config) {
		return ToolTargeting.getBlockPositions(world, target, RADIUS.get(config), SHAPE.get(config), (innerTarget, innerWorld, pos) -> true, context.filter());
	}
}
