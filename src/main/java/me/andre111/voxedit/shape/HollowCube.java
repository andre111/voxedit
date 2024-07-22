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

public class HollowCube extends Shape {
	@Override
	public boolean contains(int x, int y, int z, Direction direction, double sizeX, double sizeY, double sizeZ) {
		double xDist = Math.abs(Math.abs(x)-sizeX);
		double yDist = Math.abs(Math.abs(y)-sizeY);
		double zDist = Math.abs(Math.abs(z)-sizeZ);
		return xDist<1 || yDist<1 || zDist<1;
	}
}
