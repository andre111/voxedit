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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SetBlockAction extends EditAction {
	private final BlockPos pos;
	private final BlockState newState;
	private final BlockState oldState;
	private final NbtCompound oldNbt;

	public SetBlockAction(World world, BlockPos pos, BlockState newState) {
		this.pos = pos;
		this.newState = newState;
		
		this.oldState = world.getBlockState(pos);
		if(world.getBlockEntity(pos) != null) {
			this.oldNbt = world.getBlockEntity(pos).createNbtWithId();
		} else {
			this.oldNbt = null;
		}
	}

	@Override
	public int undo(World world) {
		// if had block entity -> first set block without be to ensure it is newly created
		if(oldNbt != null) {
            Clearable.clear(world.getBlockEntity(pos)); // do not drop anything
            world.setBlockState(pos, Blocks.BARRIER.getDefaultState(), Block.NO_REDRAW | Block.FORCE_STATE);
		}
		
		// set block back
		world.setBlockState(pos, oldState, Block.NOTIFY_LISTENERS | Block.SKIP_DROPS, 0);
		
		// restore be from stored nbt
		if(oldNbt != null && world.getBlockEntity(pos) != null) {
			world.getBlockEntity(pos).readNbt(oldNbt);
		}
		
		return 1;
	}
	
	@Override
	public int redo(World world) {
		if(world.getBlockEntity(pos) != null) {
			Clearable.clear(world.getBlockEntity(pos));
		}
		world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS | Block.SKIP_DROPS, 0);
		return 1;
	}
}
