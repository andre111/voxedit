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
package me.andre111.voxedit.editor;

import java.util.function.Consumer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class Editor {
	//TODO: combine "held" actions into one undo state
	public static int undoable(PlayerEntity player, ServerWorld world, Consumer<UndoRecordingStructureWorldAccess> edit) {
		Undo undo = Undo.of(player, world);
		UndoRecordingStructureWorldAccess worldAccess = new UndoRecordingStructureWorldAccess(world, undo);
		edit.accept(worldAccess);
		return worldAccess.apply();
	}
}
