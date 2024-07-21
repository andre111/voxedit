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

import me.andre111.voxedit.data.Context;
import me.andre111.voxedit.data.RaycastTargets;
import me.andre111.voxedit.data.Target;
import me.andre111.voxedit.data.Config;
import me.andre111.voxedit.data.CommonToolSettings;
import me.andre111.voxedit.editor.EditStats;
import me.andre111.voxedit.editor.EditType;
import me.andre111.voxedit.editor.Editor;
import me.andre111.voxedit.editor.EditorWorld;
import me.andre111.voxedit.schematic.Schematic;
import me.andre111.voxedit.state.ServerState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public abstract class VoxelTool extends Tool {
	public VoxelTool(Properties.Builder builder) {
		super(builder);
	}
	public VoxelTool(Properties properties) {
		super(properties);
	}

	@Override
	public RaycastTargets getRaycastTargets(Config config) {
		if(has(CommonToolSettings.TARGET_FLUIDS) && CommonToolSettings.TARGET_FLUIDS.get(config)) {
			return RaycastTargets.BLOCKS_AND_FLUIDS;
		} else {
			return RaycastTargets.BLOCKS_ONLY;
		}
	}

	@Override
	public void performAction(ServerPlayerEntity player, Action action, List<Target> targets, Context context, Config config, ServerState state) {
		// collect position sets
		if(targets.size() > 1 && !properties().draggable()) {
			player.sendMessage(Text.translatable("voxedit.feedback.notDraggable"), true);
			return;
		}
		List<Set<BlockPos>> positions = new ArrayList<>();
		for(int i=0; i<targets.size(); i++) {
			positions.add(getBlockPositions(player.getWorld(), targets.get(i), context, config));
		}

		// run actions
		EditStats result = EditStats.EMPTY;
		if(action == Action.PREVIEW) {
			result = Editor.undoable(player, player.getServerWorld(), asText(), (editable) -> {
				for(int i=0; i<targets.size(); i++) {
					if(positions.get(i).isEmpty()) continue;
					place(editable, player, targets.get(i), context, config, positions.get(i));
				}
			}, targets.getFirst().getBlockPos(), true);

			state.schematic("voxedit.preview."+id().toTranslationKey(), result.schematic(), true, false);
		} else if(action == Action.APPLY_PREVIEW) {
			Schematic preview = state.schematic("voxedit.preview."+id().toTranslationKey());
			if(preview == null) {
				player.sendMessage(Text.translatable("voxedit.feedback.noPreview"), true);
				return;
			}
			if(targets.size() != 1) {
				player.sendMessage(Text.translatable("voxedit.feedback.previewSinglePosition"), true);
				return;
			}

			result = Editor.undoable(player, player.getServerWorld(), asText(), (editable) -> {
				preview.apply(editable, targets.getFirst().getBlockPos());
			}, targets.getFirst().getBlockPos(), false);
		} else {
			result = Editor.undoable(player, player.getServerWorld(), asText(), (editable) -> {
				for(int i=0; i<targets.size(); i++) {
					if(positions.get(i).isEmpty()) continue;
					if(action == Action.ADD_OR_MODIFY) {
						place(editable, player, targets.get(i), context, config, positions.get(i));
					} else if(action == Action.REMOVE) {
						remove(editable, player, targets.get(i), context, config, positions.get(i));
					}
				}
			}, targets.getFirst().getBlockPos(), false);
		}
		result.inform(player, EditType.PERFORM);
	}

	public abstract void place(EditorWorld world, PlayerEntity player, Target target, Context context, Config config, Set<BlockPos> positions);
	public abstract void remove(EditorWorld world, PlayerEntity player, Target target, Context context, Config config, Set<BlockPos> positions);
	public abstract Set<BlockPos> getBlockPositions(BlockView world, Target target, Context context, Config config);
}
