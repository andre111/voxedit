package me.andre111.voxedit.editor;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface Editable {
	public void setBlock(BlockPos pos, BlockState state);
}
