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
package me.andre111.voxedit.editor;

import java.util.List;
import java.util.function.Consumer;

import me.andre111.voxedit.editor.action.EditAction;
import me.andre111.voxedit.editor.history.EditHistory;
import me.andre111.voxedit.editor.history.EditHistoryState;
import me.andre111.voxedit.network.CPHistoryInfo;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class Editor {
	public static EditStats undoable(ServerPlayerEntity player, ServerWorld world, Text text, Consumer<EditorWorld> edit, BlockPos origin, boolean preview) {
		EditHistory undo = EditHistory.of(player, world);
		EditorWorld worldAccess = new EditorWorld(world);
		edit.accept(worldAccess);
		return preview ? worldAccess.toSchematic(player, text, origin) : worldAccess.apply(player, text, undo);
	}
	
	public static EditStats undoableAction(ServerPlayerEntity player, ServerWorld world, Text text, EditAction<?> action) {
		EditHistory undo = EditHistory.of(player, world);
		
		EditStats result = new EditStats(text);
		action.redo(world, result);
		
		CPHistoryInfo info = undo.push(world, new EditHistoryState(result, List.of(action)));
		ServerPlayNetworking.send(player, info);
		
		return result;
	}
}
