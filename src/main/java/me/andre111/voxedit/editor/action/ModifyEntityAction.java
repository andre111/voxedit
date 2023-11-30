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

import me.andre111.voxedit.editor.EditStats;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class ModifyEntityAction extends EditAction {
	private final int id;
	private final NbtCompound oldNbt;
	private final NbtCompound newNbt;

	public ModifyEntityAction(int id, NbtCompound oldNbt, NbtCompound newNbt) {
		this.id = id;
		this.oldNbt = oldNbt;
		this.newNbt = newNbt;
	}

	@Override
	public void undo(World world, EditStats stats) {
		Entity entity = world.getEntityById(id);
		if(entity != null) {
			UUID uuid = entity.getUuid();
	        entity.readNbt(oldNbt);
	        entity.setUuid(uuid);
	        stats.changedEntity();
		}
	}

	@Override
	public void redo(World world, EditStats stats) {
		Entity entity = world.getEntityById(id);
		if(entity != null) {
			UUID uuid = entity.getUuid();
	        entity.readNbt(newNbt);
	        entity.setUuid(uuid);
	        stats.changedEntity();
		}
	}
}
