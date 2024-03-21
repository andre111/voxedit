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

import me.andre111.voxedit.editor.EditStats;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ModifyBlockEntityAction extends EditAction {
	private final BlockPos pos;
	private final NbtCompound oldNbt;
	private final NbtCompound newNbt;
	
	public ModifyBlockEntityAction(BlockPos pos, NbtCompound oldNbt, NbtCompound newNbt) {
		this.pos = pos;
		this.oldNbt = oldNbt;
		this.newNbt = newNbt;
	}

	@Override
	public void undo(World world, EditStats stats) {
		BlockEntity be = world.getBlockEntity(pos);
		if(be != null) {
			be.readNbt(oldNbt, world.getRegistryManager());
			stats.changedBlockEntity();
		}
	}

	@Override
	public void redo(World world, EditStats stats) {
		BlockEntity be = world.getBlockEntity(pos);
		if(be != null) {
			be.readNbt(newNbt, world.getRegistryManager());
			stats.changedBlockEntity();
		}
	}
}
