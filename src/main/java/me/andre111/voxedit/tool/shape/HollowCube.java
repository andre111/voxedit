package me.andre111.voxedit.tool.shape;

import net.minecraft.util.math.Direction;

public class HollowCube extends Shape {
	@Override
	public boolean contains(int x, int y, int z, Direction direction, int sizeX, int sizeY, int sizeZ) {
		return Math.abs(x) == sizeX || Math.abs(y) == sizeY || Math.abs(z) == sizeZ;
	}
}
