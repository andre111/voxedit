package me.andre111.voxedit.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

public record Selection(BlockPos min, BlockPos max) {
	public static final Codec<Selection> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
					BlockPos.CODEC.fieldOf("min").forGetter(Selection::min),
					BlockPos.CODEC.fieldOf("max").forGetter(Selection::max)
			)
			.apply(instance, Selection::new));
	
	public Selection expandToInclude(BlockPos p1) {
		BlockPos p2 = min;
		BlockPos p3 = max;
		
		BlockPos min = new BlockPos(Math.min(p1.getX(), Math.min(p2.getX(), p3.getX())), Math.min(p1.getY(), Math.min(p2.getY(), p3.getY())), Math.min(p1.getZ(), Math.min(p2.getZ(), p3.getZ())));
		BlockPos max = new BlockPos(Math.max(p1.getX(), Math.max(p2.getX(), p3.getX())), Math.max(p1.getY(), Math.max(p2.getY(), p3.getY())), Math.max(p1.getZ(), Math.max(p2.getZ(), p3.getZ())));
		return new Selection(min, max);
	}
	
	public BlockBox toBlockBox() {
		return BlockBox.create(min, max);
	}
}
