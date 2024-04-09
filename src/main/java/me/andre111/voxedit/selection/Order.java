package me.andre111.voxedit.selection;

import java.util.Comparator;

import net.minecraft.util.math.BlockPos;

public enum Order {
	X_MIN_TO_MAX((a, b) -> a.getX() - b.getX()),
	X_MAX_TO_MIN((a, b) -> b.getX() - a.getX()),
	Y_MIN_TO_MAX((a, b) -> a.getY() - b.getY()),
	Y_MAX_TO_MIN((a, b) -> b.getY() - a.getY()),
	Z_MIN_TO_MAX((a, b) -> a.getZ() - b.getZ()),
	Z_MAX_TO_MIN((a, b) -> b.getZ() - a.getZ());
	
	public final Comparator<BlockPos> comparator;
	
	Order(Comparator<BlockPos> comparator) {
		this.comparator = comparator;
	}
}
