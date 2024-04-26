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
package me.andre111.voxedit.tool.shape;

import net.minecraft.util.math.Direction;

public class HollowSphere extends Shape {

	@Override
	public boolean contains(int x, int y, int z, Direction direction, double sizeX, double sizeY, double sizeZ) {
		double xp = x / sizeX;
		double yp = y / sizeY;
		double zp = z / sizeZ;
		double dist = Math.sqrt(xp*xp + yp*yp + zp*zp);
		
		double edge = 0;
		xp = Math.abs(xp);
		yp = Math.abs(yp);
		zp = Math.abs(zp);
		if(xp >= yp && xp >= zp) edge = 0.5 / sizeX;
		else if(yp >= xp && yp >= zp) edge = 0.5 / sizeY;
		else edge = 0.5 / sizeZ;
		
		return (1 - edge) <= dist && dist <= (1 + edge);
	}

}
