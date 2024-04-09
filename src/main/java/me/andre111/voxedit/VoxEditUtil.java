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
package me.andre111.voxedit;

import me.andre111.voxedit.client.gui.screen.EditorScreen;
import me.andre111.voxedit.item.VoxEditItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.math.BlockPos;

public class VoxEditUtil {
	public static boolean shouldUseCustomControls(PlayerEntity player) {
		if(player != null && player.isCreative() && player.getAbilities().flying) {
			//TODO: this refers to client code -> WILL CRASH ON SERVER -> FIX!!!
			if(EditorScreen.get().isActive()) return true;
			
			ItemStack stack = player.getMainHandStack();
			return stack.getItem() instanceof VoxEditItem item && item.useCustomControls();
		}
		return false;
	}
	
	public static BlockEntity copyBlockEntity(WrapperLookup registryLookup, BlockState state, BlockEntity source, BlockPos pos) {
		// create unlinked copy and adjust position
		NbtCompound nbt = source.createNbtWithId(registryLookup);
		return BlockEntity.createFromNbt(pos, state, nbt, registryLookup);
	}
}
