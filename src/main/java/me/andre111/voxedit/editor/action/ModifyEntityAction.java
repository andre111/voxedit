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
package me.andre111.voxedit.editor.action;

import java.util.UUID;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.editor.EditStats;
import me.andre111.voxedit.editor.history.EditHistoryReader;
import me.andre111.voxedit.editor.history.EditHistoryWriter;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;

public class ModifyEntityAction extends EditAction<ModifyEntityAction> {
	private final UUID uuid;
	private final NbtCompound newNbt;
	private final NbtCompound oldNbt;

	public ModifyEntityAction(UUID uuid, NbtCompound oldNbt, NbtCompound newNbt) {
		this.uuid = uuid;
		this.oldNbt = oldNbt;
		this.newNbt = newNbt;
	}

	@Override
	public void undo(ServerWorld world, EditStats stats) {
		Entity entity = world.getEntity(uuid);
		if(entity != null) {
	        entity.readNbt(oldNbt);
	        stats.changedEntity();
		}
	}

	@Override
	public void redo(ServerWorld world, EditStats stats) {
		Entity entity = world.getEntity(uuid);
		if(entity != null) {
	        entity.readNbt(newNbt);
	        stats.changedEntity();
		}
	}

	@Override
	public Type<ModifyEntityAction> type() {
		return VoxEdit.ACTION_MODIFY_ENTITY;
	}
	
	public static void write(ModifyEntityAction action, EditHistoryWriter writer) {
		writer.writeNbt(action.newNbt);
		writer.writeNbt(action.oldNbt);
	}
	
	public static ModifyEntityAction read(EditHistoryReader reader) {
		NbtCompound newNbt = reader.readNbt();
		NbtCompound oldNbt = reader.readNbt();
		UUID uuid = oldNbt.getUuid("UUID");
		return new ModifyEntityAction(uuid, oldNbt, newNbt);
	}
}
