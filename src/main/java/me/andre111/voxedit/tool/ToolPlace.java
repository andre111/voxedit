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
import me.andre111.voxedit.tool.data.Target;
import me.andre111.voxedit.tool.data.ToolConfig;
import me.andre111.voxedit.tool.data.ToolSetting;
import me.andre111.voxedit.tool.data.ToolSettings;
import me.andre111.voxedit.tool.data.ToolTargeting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public class ToolPlace extends Tool {
	private static final ToolSetting<Identifier> FEATURE = ToolSetting.ofIdentifier("feature", new Identifier("minecraft", "oak"), RegistryKeys.CONFIGURED_FEATURE);
	private static final ToolSetting<Integer> TRIES = ToolSetting.ofInt("tries", 3, 1, 10);
	
	public ToolPlace() {
		super(Properties.of(FEATURE, TRIES, ToolSettings.TARGET_FLUIDS).showPreview());
	}

	@Override
	public void place(EditorWorld world, PlayerEntity player, Target target, Context context, ToolConfig config, Set<BlockPos> positions) {
		ConfiguredFeature<?, ?> configuredFeature = world.getRegistryManager().get(RegistryKeys.CONFIGURED_FEATURE).get(FEATURE.get(config));
		if(configuredFeature == null) return;
		
		BlockPos pos = positions.iterator().next();
		for(int t=0; t<TRIES.get(config); t++) {
	        if(configuredFeature.generate(world, world.getChunkManager().getChunkGenerator(), world.getRandom(), pos)) break;
		}
	}

	@Override
	public void remove(EditorWorld world, PlayerEntity player, Target target, Context context, ToolConfig config, Set<BlockPos> positions) {
	}

	@Override
	public Set<BlockPos> getBlockPositions(BlockView world, Target target, Context context, ToolConfig config) {
		if(ToolTargeting.isFree(world, target.pos())) return Set.of();
		BlockPos offset = target.pos().offset(target.side());
		if(!ToolTargeting.isFree(world, offset)) return Set.of();
		return Set.of(offset);
	}
}
