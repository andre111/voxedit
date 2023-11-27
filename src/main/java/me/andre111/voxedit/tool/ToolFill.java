package me.andre111.voxedit.tool;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import me.andre111.voxedit.ToolState;
import me.andre111.voxedit.editor.Editor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ToolFill extends Tool {
	@Override
	public void rightClick(World world, PlayerEntity player, BlockHitResult target, ToolState state, Set<BlockPos> positions) {
		Editor.undoable(player, world, editable -> {
			for(BlockPos pos : getBlockPositions(world, target, state)) {
				editable.setBlock(pos, state.palette().getRandom(world.getRandom()));
			}
		});
	}

	@Override
	public void leftClick(World world, PlayerEntity player, BlockHitResult target, ToolState state, Set<BlockPos> positions) {
	}

	@Override
	public Set<BlockPos> getBlockPositions(World world, BlockHitResult target, ToolState state) {
		Set<BlockPos> positions = new HashSet<>();
		
		BlockPos center = target.getBlockPos();
		if(!shouldFill(world.getBlockState(center), null, state)) return positions;
		
		Block targetBlock = world.getBlockState(center).getBlock();	
		Queue<BlockPos> checkNeighbors = new LinkedList<>();
		
		positions.add(center);
		checkNeighbors.add(center);
		
		while(!checkNeighbors.isEmpty()) {
			BlockPos pos = checkNeighbors.poll();
			for(Direction dir : Direction.values()) {
				BlockPos neighbor = pos.offset(dir);
				if(Math.abs(neighbor.getX() - center.getX()) > state.radius()) continue;
				if(Math.abs(neighbor.getY() - center.getY()) > state.radius()) continue;
				if(Math.abs(neighbor.getZ() - center.getZ()) > state.radius()) continue;
				if(!shouldFill(world.getBlockState(neighbor), targetBlock, state)) continue;
				if(positions.contains(neighbor)) continue;
				if(checkNeighbors.contains(neighbor)) continue;
				
				positions.add(neighbor);
				checkNeighbors.add(neighbor);
			}
		}
		
		return positions;
	}
	
	private boolean shouldFill(BlockState blockState, Block targetBlock, ToolState state) {
		if(state.filter().size() == 0) {
			if(targetBlock == null) return !blockState.isAir();
			else return blockState.getBlock() == targetBlock;
		} else {
			return state.passesFilter(blockState);
		}
	}
	
	@Override
	public boolean usesMode() {
		return false;
	}
	
	@Override
	public boolean usesShape() {
		return false;
	}

	@Override
	public List<ToolState> getCreativeMenuStates() {
		return List.of(ToolState.of(this).withRadius(16));
	}
}
