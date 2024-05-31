package me.andre111.voxedit.filter;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public record FilterContext(BlockView view, BlockPos pos) {
	public FilterContext withPos(BlockPos pos) {
		return new FilterContext(view, pos);
	}
}
