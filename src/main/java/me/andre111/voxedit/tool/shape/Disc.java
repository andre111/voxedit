package me.andre111.voxedit.tool.shape;

import net.minecraft.util.math.Direction;

public class Disc extends Shape {

	@Override
	public boolean contains(int x, int y, int z, Direction direction, int sizeX, int sizeY, int sizeZ) {
		double xp = x / (double) sizeX;
		double yp = y / (double) sizeY;
		double zp = z / (double) sizeZ;
		if(Math.sqrt(xp*xp + yp*yp + zp*zp) > 1) return false;
		if(direction.getOffsetX() != 0 && x != 0) return false;
		if(direction.getOffsetY() != 0 && y != 0) return false;
		if(direction.getOffsetZ() != 0 && z != 0) return false;
		return true;
	}

}
