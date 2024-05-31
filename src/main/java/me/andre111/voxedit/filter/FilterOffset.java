package me.andre111.voxedit.filter;

import net.minecraft.util.math.BlockPos;

public record FilterOffset(Filter filter, BlockPos offset) implements Filter {
	@Override
	public boolean check(FilterContext context) {
		return filter.check(context.withPos(context.pos().add(offset)));
	}
}
