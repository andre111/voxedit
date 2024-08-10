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
package me.andre111.voxedit.editor.action;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.editor.EditStats;
import me.andre111.voxedit.editor.history.EditHistoryReader;
import me.andre111.voxedit.editor.history.EditHistoryWriter;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;

public class ModifyBlockEntityAction extends EditAction<ModifyBlockEntityAction> {
	private final BlockPos pos;
	private final NbtCompound newNbt;
	private final NbtCompound oldNbt;
	
	public ModifyBlockEntityAction(BlockPos pos, NbtCompound oldNbt, NbtCompound newNbt) {
		this.pos = pos;
		this.oldNbt = oldNbt;
		this.newNbt = newNbt;
	}

	@Override
	public void undo(StructureWorldAccess world, EditStats stats) {
		BlockEntity be = world.getBlockEntity(pos);
		if(be != null) {
			be.read(oldNbt, world.getRegistryManager());
			stats.changedBlockEntity();
		}
	}

	@Override
	public void redo(StructureWorldAccess world, EditStats stats) {
		BlockEntity be = world.getBlockEntity(pos);
		if(be != null) {
			be.read(newNbt, world.getRegistryManager());
			stats.changedBlockEntity();
		}
	}

	@Override
	public Type<ModifyBlockEntityAction> type() {
		return VoxEdit.ACTION_MODIFY_BLOCK_ENTITY;
	}
	
	public static void write(ModifyBlockEntityAction action, EditHistoryWriter writer) {
		writer.writeBlockPos(action.pos);
		writer.writeNbt(action.newNbt);
		writer.writeNbt(action.oldNbt);
	}
	
	public static ModifyBlockEntityAction read(EditHistoryReader reader) {
		BlockPos pos = reader.readBlockPos();
		NbtCompound newNbt = reader.readNbt();
		NbtCompound oldNbt = reader.readNbt();
		return new ModifyBlockEntityAction(pos, oldNbt, newNbt);
	}
}
