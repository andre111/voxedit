package me.andre111.voxedit.client.data;

import net.minecraft.util.math.BlockPos;

public interface Positionable {
	public BlockPos getPos();
	public void setPos(BlockPos pos);
}
