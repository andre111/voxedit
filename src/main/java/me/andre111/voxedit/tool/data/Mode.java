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
package me.andre111.voxedit.tool.data;

import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public enum Mode {
	SOLID((target, world, pos) -> true),
	PAINT((target, world, pos) -> {
		if(Selection.isFree(world, pos)) return false;
		for(Direction d : Direction.values()) {
			if(Selection.isFree(world, pos.offset(d))) return true;
		}
		return false;
	}),
	PAINT_TOP((target, world, pos) -> {
		if(Selection.isFree(world, pos)) return false;
		return Selection.isFree(world, pos.offset(Direction.UP));
	}),
	SCATTER((target, world, pos) -> {
		if(!world.getBlockState(pos).isAir()) return false;
		return !Selection.isFree(world, pos.offset(target.getSide().getOpposite()));
	});
	
	public final TestPredicate testPredicate;
	
	Mode(TestPredicate testPredicate) {
		this.testPredicate = testPredicate;
	}
	
	public Text asText() {
		return Text.translatable("voxedit.mode."+name().toLowerCase());
	}
	
	public static interface TestPredicate {
		public boolean test(BlockHitResult target, BlockView world, BlockPos pos);
	}
}
