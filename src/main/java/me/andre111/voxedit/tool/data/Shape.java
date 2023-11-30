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

public enum Shape {
	SPHERE((target, radius, x, y, z) -> Math.sqrt(x*x + y*y + z*z) <= radius),
	CUBE((target, radius, x, y, z) -> true),
	DISC((target, radius, x, y, z) -> {
		if(Math.sqrt(x*x + y*y + z*z) > radius) return false;
		if(target.getSide().getOffsetX() != 0 && x != 0) return false;
		if(target.getSide().getOffsetY() != 0 && y != 0) return false;
		if(target.getSide().getOffsetZ() != 0 && z != 0) return false;
		return true;
	}),
	CYLINDER((target, radius, x, y, z) -> {
		if(target.getSide().getOffsetX() != 0 && Math.sqrt(y*y + z*z) > radius) return false;
		if(target.getSide().getOffsetY() != 0 && Math.sqrt(x*x + z*z) > radius) return false;
		if(target.getSide().getOffsetZ() != 0 && Math.sqrt(x*x + y*y) > radius) return false;
		return true;
	}),
	HOLLOW_SPHERE((target, radius, x, y, z) -> {
		double dist = Math.sqrt(x*x + y*y + z*z);
		return (radius - 0.5) <= dist && dist <= (radius + 0.5);
	}),
	HOLLOW_CUBE((target, radius, x, y, z) -> Math.abs(x) == radius || Math.abs(y) == radius || Math.abs(z) == radius);
	
	final OffsetPredicate offsetPredicate;
	
	Shape(OffsetPredicate offsetPredicate) {
		this.offsetPredicate = offsetPredicate;
	}
	
	public Text asText() {
		return Text.translatable("voxedit.shape."+name().toLowerCase());
	}
	
	public static interface OffsetPredicate {
		public boolean test(BlockHitResult target, int radius, int x, int y, int z);
	}
}
