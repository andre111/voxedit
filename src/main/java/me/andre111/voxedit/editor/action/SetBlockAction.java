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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

//TODO: store and restore blockentities!
//TODO: somehow make other blocks not update/drop as items?
public class SetBlockAction extends EditAction {
	private final BlockPos pos;
	private final BlockState oldState;
	private final BlockState newState;

	public SetBlockAction(World world, BlockPos pos, BlockState newState) {
		this.pos = pos;
		this.oldState = world.getBlockState(pos);
		this.newState = newState;
	}

	@Override
	public int undo(World world) {
		world.setBlockState(pos, oldState, Block.NOTIFY_LISTENERS | Block.SKIP_DROPS, 0);
		return 1;
	}
	
	@Override
	public int redo(World world) {
		world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS | Block.SKIP_DROPS, 0);
		return 1;
	}
}
