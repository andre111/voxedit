package me.andre111.voxedit.filter;

import me.andre111.voxedit.tool.data.BlockPalette;
import net.minecraft.block.BlockState;

public record FilterBlock(BlockPalette palette) implements Filter {
	@Override
	public boolean check(FilterContext context) {
		BlockState state = context.view().getBlockState(context.pos());
		return palette.has(state);
	}
}
