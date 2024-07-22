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

import java.util.Map;
import java.util.Set;

import me.andre111.voxedit.data.Context;
import me.andre111.voxedit.data.Setting;
import me.andre111.voxedit.data.Size;
import me.andre111.voxedit.data.Target;
import me.andre111.voxedit.data.Config;
import me.andre111.voxedit.data.CommonToolSettings;
import me.andre111.voxedit.data.ToolTargeting;
import me.andre111.voxedit.editor.EditorWorld;
import me.andre111.voxedit.shape.Shape;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class ToolScatter extends VoxelTool {
	private static final Setting<Boolean> CHECK_CAN_PLACE = Setting.ofBoolean("checkCanPlace", false);
	
	public ToolScatter() {
		super(Properties.of(CommonToolSettings.SHAPE, CHECK_CAN_PLACE, CommonToolSettings.TARGET_FLUIDS).draggable());
	}

	@Override
	public void place(EditorWorld world, PlayerEntity player, Target target, Context context, Config config, Set<BlockPos> positions) {
		boolean checkCanPlace = CHECK_CAN_PLACE.get(config);
		for(BlockPos pos : positions) {
			BlockState state = context.palette().getRandom(world.getRandom());
			if(!checkCanPlace || state.canPlaceAt(world, pos)) world.setBlockState(pos, state, 0);
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
		return ToolTargeting.getBlockPositions(world, target, CommonToolSettings.SHAPE.get(config), (innerTarget, innerWorld, pos) -> {
			if(!innerWorld.getBlockState(pos).isAir()) return false;
			return !ToolTargeting.isFree(innerWorld, pos.offset(innerTarget.getSide().getOpposite()));
		}, context.filter());
	}
	
	@Override
	public Map<String, Config> getPresets() {
		return Map.of("Place Valid", getDefaultConfig().modify(CommonToolSettings.SHAPE, s -> s.with(s.config().modify(Shape.SIZE, size -> new Size(true, false, 6, 6, 6)))).with(CHECK_CAN_PLACE, true));
	}
}
