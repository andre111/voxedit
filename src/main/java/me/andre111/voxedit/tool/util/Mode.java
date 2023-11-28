package me.andre111.voxedit.tool.util;

import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public enum Mode {
	SOLID((target, world, pos) -> true),
	PAINT((target, world, pos) -> {
		if(Defaults.isFree(world, pos)) return false;
		for(Direction d : Direction.values()) {
			if(Defaults.isFree(world, pos.offset(d))) return true;
		}
		return false;
	}),
	PAINT_TOP((target, world, pos) -> {
		if(Defaults.isFree(world, pos)) return false;
		return Defaults.isFree(world, pos.offset(Direction.UP));
	}),
	SCATTER((target, world, pos) -> {
		if(!world.getBlockState(pos).isAir()) return false;
		return !Defaults.isFree(world, pos.offset(target.getSide().getOpposite()));
	});
	
	public final TestPredicate testPredicate;
	
	Mode(TestPredicate testPredicate) {
		this.testPredicate = testPredicate;
	}
	
	public Text asText() {
		return Text.of(name());
	}
	
	public static interface TestPredicate {
		public boolean test(BlockHitResult target, BlockView world, BlockPos pos);
	}
}
