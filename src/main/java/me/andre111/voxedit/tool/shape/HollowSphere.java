package me.andre111.voxedit.tool.shape;

import net.minecraft.util.math.Direction;

public class HollowSphere extends Shape {

	@Override
	public boolean contains(int x, int y, int z, Direction direction, int sizeX, int sizeY, int sizeZ) {
		double xp = x / (double) sizeX;
		double yp = y / (double) sizeY;
		double zp = z / (double) sizeZ;
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
