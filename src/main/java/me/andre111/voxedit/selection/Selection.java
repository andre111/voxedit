package me.andre111.voxedit.selection;

import java.util.Iterator;

import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

public interface Selection {
	public boolean contains(BlockPos pos);
	public BlockBox getBoundingBox();
	public Iterator<BlockPos> iterator(Order order);
}
