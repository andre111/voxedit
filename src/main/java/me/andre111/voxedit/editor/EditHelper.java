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

import me.andre111.voxedit.selection.Order;
import me.andre111.voxedit.selection.Selection;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class EditHelper {
	public static void move(EditorWorld world, Selection selection, int offsetX, int offsetY, int offsetZ) {
		if(offsetX == 0 && offsetY == 0 && offsetZ == 0) return;

		Order order = null;
		if(offsetX > 0) order = Order.X_MAX_TO_MIN;
		else if(offsetX < 0) order = Order.X_MIN_TO_MAX;
		else if(offsetY > 0) order = Order.Y_MAX_TO_MIN;
		else if(offsetY < 0) order = Order.Y_MIN_TO_MAX;
		else if(offsetZ > 0) order = Order.Z_MAX_TO_MIN;
		else if(offsetZ < 0) order = Order.Z_MIN_TO_MAX;
		
		selection.iterator(order).forEachRemaining(pos -> {
			BlockPos target = pos.add(offsetX, offsetY, offsetZ);
			BlockState state = world.getBlockState(pos);
			world.setBlockState(target, state, 0);
			if(state.hasBlockEntity()) {
				BlockEntity sourceBE = world.getBlockEntity(pos);
				BlockEntity targetBE = world.getBlockEntity(target);
				targetBE.read(sourceBE.createNbt(world.getRegistryManager()), world.getRegistryManager());
			}
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 0);
		});
	}
}
