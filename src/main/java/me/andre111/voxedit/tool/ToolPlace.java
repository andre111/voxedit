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
import me.andre111.voxedit.tool.config.ToolConfigPlace;
import me.andre111.voxedit.tool.data.Selection;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public class ToolPlace extends Tool<ToolConfigPlace, ToolPlace> {
	public ToolPlace() {
		super(ToolConfigPlace.CODEC, new ToolConfigPlace());
	}

	@Override
	public void rightClick(UndoRecordingStructureWorldAccess world, PlayerEntity player, BlockHitResult target, ToolConfigPlace config, Set<BlockPos> positions) {
		ConfiguredFeature<?, ?> configuredFeature = world.getRegistryManager().get(RegistryKeys.CONFIGURED_FEATURE).get(config.feature());
		if(configuredFeature == null) return;
		
		BlockPos pos = positions.iterator().next();
		for(int t=0; t<config.tries(); t++) {
	        if(configuredFeature.generate(world, world.getChunkManager().getChunkGenerator(), world.getRandom(), pos)) break;
		}
	}

	@Override
	public void leftClick(UndoRecordingStructureWorldAccess world, PlayerEntity player, BlockHitResult target, ToolConfigPlace config, Set<BlockPos> positions) {
	}

	@Override
	public Set<BlockPos> getBlockPositions(BlockView world, BlockHitResult target, ToolConfigPlace config) {
		BlockPos targetPos = target.getBlockPos();
		if(Selection.isFree(world, targetPos)) return Set.of();
		BlockPos up = targetPos.offset(target.getSide());
		if(!Selection.isFree(world, up)) return Set.of();
		return Set.of(up);
	}
}
