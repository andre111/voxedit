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

public class ToolPlace extends VoxelTool {
	private static final ToolSetting<Identifier> FEATURE = ToolSetting.ofIdentifier("feature", Identifier.of("minecraft", "oak"), RegistryKeys.CONFIGURED_FEATURE);
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
		if(ToolTargeting.isFree(world, target.getBlockPos())) return Set.of();
		BlockPos offset = target.getBlockPos().offset(target.getSide());
		if(!ToolTargeting.isFree(world, offset)) return Set.of();
		return Set.of(offset);
	}
	
	@Override
	public Map<String, ToolConfig> getPresets() {
		return Map.of(
				"Oak", getDefaultConfig().with(FEATURE, Identifier.of("minecraft", "oak")),
				"Birch", getDefaultConfig().with(FEATURE, Identifier.of("minecraft", "birch")),
				"Spruce", getDefaultConfig().with(FEATURE, Identifier.of("minecraft", "spruce")),
				"Acacia", getDefaultConfig().with(FEATURE, Identifier.of("minecraft", "acacia")),
				"Dark Oak", getDefaultConfig().with(FEATURE, Identifier.of("minecraft", "dark_oak")),
				"Jungle", getDefaultConfig().with(FEATURE, Identifier.of("minecraft", "jungle_tree")),
				"Azalea", getDefaultConfig().with(FEATURE, Identifier.of("minecraft", "azalea_tree")),
				"Mangrove", getDefaultConfig().with(FEATURE, Identifier.of("minecraft", "mangrove"))
			);
	}
}
