package me.andre111.voxedit.tool.util;

import java.util.HashSet;
import java.util.Set;

import me.andre111.voxedit.BlockPalette;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class Defaults {
	public static Set<BlockPos> getBlockPositions(BlockView world, BlockHitResult target, int radius) {
		return getBlockPositions(world, target, radius, null, null, null);
	}
	public static Set<BlockPos> getBlockPositions(BlockView world, BlockHitResult target, int radius, Shape shape) {
		return getBlockPositions(world, target, radius, shape, null, null);
	}
	public static Set<BlockPos> getBlockPositions(BlockView world, BlockHitResult target, int radius, Shape shape, Mode.TestPredicate testPredicate) {
		return getBlockPositions(world, target, radius, shape, testPredicate, null);
	}
	public static Set<BlockPos> getBlockPositions(BlockView world, BlockHitResult target, int radius, Shape shape, Mode.TestPredicate testPredicate, BlockPalette filter) {
		Set<BlockPos> positions = new HashSet<>();
		
		BlockPos center = target.getBlockPos();
		BlockPos.Mutable pos = new BlockPos.Mutable();
		for(int x = -radius; x <= radius; x++) {
        	for(int y = -radius; y <= radius; y++) {
        		for(int z = -radius; z <= radius; z++) {
                	if(shape != null && !shape.offsetPredicate.test(target, radius, x, y, z)) continue;
                	
                	pos.set(center.getX()+x, center.getY()+y, center.getZ()+z);
                	
                	if(testPredicate != null && !testPredicate.test(target, world, pos)) continue;
                	if(filter != null && filter.size() > 0 && !filter.has(world.getBlockState(pos).getBlock())) continue;
                	
        			positions.add(pos.toImmutable());
                }
            }
        }
		
		return positions;
	}
	
	public static boolean isFree(BlockView world, BlockPos pos) {
		return world.getBlockState(pos).getCollisionShape(world, pos).isEmpty();
	}
}
