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
import me.andre111.voxedit.tool.config.ToolConfigBrush;
import me.andre111.voxedit.tool.data.BlockPalette;
import me.andre111.voxedit.tool.data.Selection;
import me.andre111.voxedit.tool.data.Mode;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class ToolBrush extends Tool<ToolConfigBrush, ToolBrush> {
	public ToolBrush() {
		super(ToolConfigBrush.CODEC, new ToolConfigBrush());
	}

	@Override
	public void rightClick(UndoRecordingStructureWorldAccess world, PlayerEntity player, BlockHitResult target, ToolConfigBrush config, Set<BlockPos> positions) {
		for(BlockPos pos : positions) {
			BlockState state = config.palette().getRandom(world.getRandom());
			if(!config.checkCanPlace() || state.canPlaceAt(world, pos)) world.setBlockState(pos, state, 0);
		}
	}

	@Override
	public void leftClick(UndoRecordingStructureWorldAccess world, PlayerEntity player, BlockHitResult target, ToolConfigBrush config, Set<BlockPos> positions) {
		for(BlockPos pos : positions) {
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 0);
		}
	}

	@Override
	public Set<BlockPos> getBlockPositions(BlockView world, BlockHitResult target, ToolConfigBrush config) {
		return Selection.getBlockPositions(world, target, config.radius(), config.shape(), config.mode().testPredicate, config.filter());
	}

	public List<ToolConfigBrush> getAdditionalCreativeMenuConfigs() {
		return List.of(
				getDefaultConfig().withMode(Mode.PAINT_TOP).withRadius(5).withPalette(new BlockPalette(Util.make(new ArrayList<>(), list -> {
					list.add(new BlockPalette.Entry(Blocks.DIRT.getDefaultState(), 1));
					list.add(new BlockPalette.Entry(Blocks.GRAVEL.getDefaultState(), 1));
					list.add(new BlockPalette.Entry(Blocks.DIRT_PATH.getDefaultState(), 1));
				}))),
				getDefaultConfig().withMode(Mode.SCATTER).withRadius(6).withPalette(new BlockPalette(Util.make(new ArrayList<>(), list -> {
					list.add(new BlockPalette.Entry(Blocks.AIR.getDefaultState(), 20));
					list.add(new BlockPalette.Entry(Blocks.SHORT_GRASS.getDefaultState(), 5));
					list.add(new BlockPalette.Entry(Blocks.FERN.getDefaultState(), 5));
					list.add(new BlockPalette.Entry(Blocks.POPPY.getDefaultState(), 1));
					list.add(new BlockPalette.Entry(Blocks.DANDELION.getDefaultState(), 1));
				})))
				);
	}
}
