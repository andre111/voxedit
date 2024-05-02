package me.andre111.voxedit.client.gizmo;

import net.minecraft.util.math.BlockPos;

public interface Sizeable {
	public BlockPos getSize();
	public void setSize(BlockPos size);
}
