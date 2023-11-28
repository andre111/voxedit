package me.andre111.voxedit.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.andre111.voxedit.editor.UndoRecordingStructureWorldAccess;
import me.andre111.voxedit.tool.config.ToolConfigBlend;
import me.andre111.voxedit.tool.util.Defaults;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class ToolBlend extends Tool<ToolConfigBlend, ToolBlend> {
	public ToolBlend() {
		super(ToolConfigBlend.CODEC, new ToolConfigBlend());
	}

	@Override
	public void rightClick(UndoRecordingStructureWorldAccess world, PlayerEntity player, BlockHitResult target, ToolConfigBlend config, Set<BlockPos> positions) {
		List<BlockState> neighbors = new ArrayList<>();
		for(BlockPos pos : positions) {
			// find all solid neighbors
			neighbors.clear();
			for(Direction dir : Direction.values()) {
				BlockPos offset = pos.offset(dir);
				if(positions.contains(offset) && !Defaults.isFree(world, offset)) neighbors.add(world.getBlockState(offset));
			}
			if(neighbors.isEmpty()) continue;
			
			// select random neighbor
			BlockState newBlockState = neighbors.get(world.getRandom().nextInt(neighbors.size()));
			world.setBlockState(pos, newBlockState, 0);
		}
	}

	@Override
	public void leftClick(UndoRecordingStructureWorldAccess world, PlayerEntity player, BlockHitResult target, ToolConfigBlend config, Set<BlockPos> positions) {
	}

	@Override
	public Set<BlockPos> getBlockPositions(BlockView world, BlockHitResult target, ToolConfigBlend config) {
		BlockPos center = target.getBlockPos();
		if(Defaults.isFree(world, center)) return Set.of();
		
		return Defaults.getBlockPositions(world, target, config.radius(), config.shape(), (hit, testWorld, testPos) -> !Defaults.isFree(testWorld, testPos), config.filter());
	}
}
