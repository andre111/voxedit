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
package me.andre111.voxedit.shape;

import net.minecraft.util.math.Direction;

public class Disc extends Shape {
	@Override
	public boolean contains(int x, int y, int z, Direction direction, double sizeX, double sizeY, double sizeZ) {
		double xp = x / sizeX;
		double yp = y / sizeY;
		double zp = z / sizeZ;
		if(Math.sqrt(xp*xp + yp*yp + zp*zp) > 1) return false;
		if(direction.getOffsetX() != 0 && x != 0) return false;
		if(direction.getOffsetY() != 0 && y != 0) return false;
		if(direction.getOffsetZ() != 0 && z != 0) return false;
		return true;
	}
}
