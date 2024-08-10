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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;

public class SetBlockAction extends EditAction<SetBlockAction> {
	private final BlockPos pos;
	private final BlockState newState;
	private final BlockState oldState;
	private final NbtCompound oldNbt;

	public SetBlockAction(StructureWorldAccess world, BlockPos pos, BlockState newState) {
		this.pos = pos;
		this.newState = newState;
		
		this.oldState = world.getBlockState(pos);
		if(world.getBlockEntity(pos) != null) {
			this.oldNbt = world.getBlockEntity(pos).createNbtWithId(world.getRegistryManager());
		} else {
			this.oldNbt = null;
		}
	}
	
	private SetBlockAction(BlockPos pos, BlockState newState, BlockState oldState, NbtCompound oldNbt) {
		this.pos = pos;
		this.newState = newState;
		this.oldState = oldState;
		this.oldNbt = oldNbt;
	}

	@Override
	public void undo(StructureWorldAccess world, EditStats stats) {
		// if had block entity -> first set block without to ensure it is newly created
		if(oldNbt != null) {
            Clearable.clear(world.getBlockEntity(pos)); // do not drop anything
            world.setBlockState(pos, Blocks.BARRIER.getDefaultState(), Block.NO_REDRAW | Block.FORCE_STATE);
		}
		
		// restore blockstate
		world.setBlockState(pos, oldState, Block.NOTIFY_LISTENERS | Block.SKIP_DROPS, 0);
		
		// restore be from stored nbt
		if(oldNbt != null && world.getBlockEntity(pos) != null) {
			world.getBlockEntity(pos).read(oldNbt, world.getRegistryManager());
		}
		
		stats.changedBlock();
	}
	
	@Override
	public void redo(StructureWorldAccess world, EditStats stats) {
		if(world.getBlockEntity(pos) != null) {
			Clearable.clear(world.getBlockEntity(pos));
		}
		world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS | Block.SKIP_DROPS, 0);
		stats.changedBlock();
	}

	@Override
	public Type<SetBlockAction> type() {
		return VoxEdit.ACTION_SET_BLOCK;
	}
	
	public static void write(SetBlockAction action, EditHistoryWriter writer) {
		writer.writeBlockPos(action.pos);
		writer.writeBlockState(action.newState);
		writer.writeBlockState(action.oldState);
		writer.writeFlag(action.oldNbt != null);
		if(action.oldNbt != null) writer.writeNbt(action.oldNbt);
	}
	
	public static SetBlockAction read(EditHistoryReader reader) {
		BlockPos pos = reader.readBlockPos();
		BlockState newState = reader.readBlockState();
		BlockState oldState = reader.readBlockState();
		NbtCompound oldNbt = null;
		if(reader.readFlagBoolean()) oldNbt = reader.readNbt();
		return new SetBlockAction(pos, newState, oldState, oldNbt);
	}
}
