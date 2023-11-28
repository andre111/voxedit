package me.andre111.voxedit.tool;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import me.andre111.voxedit.editor.UndoRecordingStructureWorldAccess;
import me.andre111.voxedit.tool.config.ToolConfigFill;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class ToolFill extends Tool<ToolConfigFill, ToolFill> {
	public ToolFill() {
		super(ToolConfigFill.CODEC, new ToolConfigFill());
	}

	@Override
	public void rightClick(UndoRecordingStructureWorldAccess world, PlayerEntity player, BlockHitResult target, ToolConfigFill config, Set<BlockPos> positions) {
		for(BlockPos pos : positions) {
			world.setBlockState(pos, config.palette().getRandom(world.getRandom()), 0);
		}
	}

	@Override
	public void leftClick(UndoRecordingStructureWorldAccess world, PlayerEntity player, BlockHitResult target, ToolConfigFill config, Set<BlockPos> positions) {
	}

	@Override
	public Set<BlockPos> getBlockPositions(BlockView world, BlockHitResult target, ToolConfigFill config) {
		Set<BlockPos> positions = new HashSet<>();
		
		BlockPos center = target.getBlockPos();
		if(!shouldFill(world.getBlockState(center), null, config)) return positions;
		
		Block targetBlock = world.getBlockState(center).getBlock();	
		Queue<BlockPos> checkNeighbors = new LinkedList<>();
		
		positions.add(center);
		checkNeighbors.add(center);
		
		while(!checkNeighbors.isEmpty()) {
			BlockPos pos = checkNeighbors.poll();
			for(Direction dir : Direction.values()) {
				BlockPos neighbor = pos.offset(dir);
				if(Math.abs(neighbor.getX() - center.getX()) > config.radius()) continue;
				if(Math.abs(neighbor.getY() - center.getY()) > config.radius()) continue;
				if(Math.abs(neighbor.getZ() - center.getZ()) > config.radius()) continue;
				if(!shouldFill(world.getBlockState(neighbor), targetBlock, config)) continue;
				if(positions.contains(neighbor)) continue;
				if(checkNeighbors.contains(neighbor)) continue;
				
				positions.add(neighbor);
				checkNeighbors.add(neighbor);
			}
		}
		
		return positions;
	}
	
	private boolean shouldFill(BlockState blockState, Block targetBlock, ToolConfigFill config) {
		if(config.filter().size() == 0) {
			if(targetBlock == null) return !blockState.isAir();
			else return blockState.getBlock() == targetBlock;
		} else {
			return config.filter().has(blockState.getBlock());
		}
	}
}
