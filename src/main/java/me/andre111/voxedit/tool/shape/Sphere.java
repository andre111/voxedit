package me.andre111.voxedit.tool.shape;

import net.minecraft.util.math.Direction;

public class Sphere extends Shape {
	@Override
	public boolean contains(int x, int y, int z, Direction direction, int sizeX, int sizeY, int sizeZ) {
		double xp = x / (double) sizeX;
		double yp = y / (double) sizeY;
		double zp = z / (double) sizeZ;
		//return Math.sqrt(xp*xp + yp*yp + zp*zp) <= 1;
		return xp*xp + yp*yp + zp*zp <= 1;
	}
}
