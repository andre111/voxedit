package me.andre111.voxedit.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.andre111.voxedit.ToolState;
import me.andre111.voxedit.editor.Editor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ToolBlend extends Tool {
	@Override
	public int rightClick(World world, PlayerEntity player, BlockHitResult target, ToolState state, Set<BlockPos> positions) {
		return Editor.undoable(player, world, editable -> {
			List<BlockState> neighbors = new ArrayList<>();
			for(BlockPos pos : getBlockPositions(world, target, state)) {
				// find all solid neighbors
				neighbors.clear();
				for(Direction dir : Direction.values()) {
					BlockPos offset = pos.offset(dir);
					if(!ToolState.isFree(world, offset)) neighbors.add(world.getBlockState(offset));
				}
				if(neighbors.isEmpty()) continue;
				
				// select random neighbor
				BlockState newBlockState = neighbors.get(world.getRandom().nextInt(neighbors.size()));
				editable.setBlock(pos, newBlockState);
			}
		});
	}

	@Override
	public int leftClick(World world, PlayerEntity player, BlockHitResult target, ToolState state, Set<BlockPos> positions) {
		return 0;
	}

	@Override
	public Set<BlockPos> getBlockPositions(World world, BlockHitResult target, ToolState state) {
		BlockPos center = target.getBlockPos();
		if(ToolState.isFree(world, center)) return Set.of();
		
		Set<BlockPos> positions = super.getBlockPositions(world, target, state);
		positions.removeIf(pos -> ToolState.isFree(world, pos));
		return positions;
	}
	
	@Override
	public boolean usesMode() {
		return false;
	}
	
	@Override
	public boolean usesBlockPalette() {
		return false;
	}

	@Override
	public List<ToolState> getCreativeMenuStates() {
		return List.of(ToolState.of(this).withShape(ToolState.Shape.DISC).withRadius(5));
	}
}
