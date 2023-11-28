package me.andre111.voxedit.tool;

import java.util.Set;

import me.andre111.voxedit.editor.UndoRecordingStructureWorldAccess;
import me.andre111.voxedit.tool.config.ToolConfigFlatten;
import me.andre111.voxedit.tool.util.Defaults;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class ToolFlatten extends Tool<ToolConfigFlatten, ToolFlatten> {
	public ToolFlatten() {
		super(ToolConfigFlatten.CODEC, new ToolConfigFlatten());
	}

	@Override
	public void rightClick(UndoRecordingStructureWorldAccess world, PlayerEntity player, BlockHitResult target, ToolConfigFlatten config, Set<BlockPos> positions) {
		for(BlockPos pos : positions) {
			if(Defaults.isFree(world, pos)) {
				world.setBlockState(pos, config.palette().getRandom(world.getRandom()), 0);
			} else {
				world.setBlockState(pos, Blocks.AIR.getDefaultState(), 0);
			}
		}
	}

	@Override
	public void leftClick(UndoRecordingStructureWorldAccess world, PlayerEntity player, BlockHitResult target, ToolConfigFlatten config, Set<BlockPos> positions) {
	}

	@Override
	public Set<BlockPos> getBlockPositions(BlockView world, BlockHitResult target, ToolConfigFlatten config) {
		BlockPos center = target.getBlockPos();
		if(Defaults.isFree(world, center)) return Set.of();
		
		Set<BlockPos> positions = Defaults.getBlockPositions(world, target, config.radius(), config.shape());
		positions.removeIf(pos -> {
			int offset = switch(target.getSide()) {
			case UP -> pos.getY() - center.getY();
			case DOWN -> center.getY() - pos.getY();
			case SOUTH -> pos.getZ() - center.getZ();
			case NORTH -> center.getZ() - pos.getZ();
			case EAST -> pos.getX() - center.getX();
			case WEST -> center.getX() - pos.getX();
			};
			if(offset <= 0) return !Defaults.isFree(world, pos);
			else return Defaults.isFree(world, pos);
		});
		return positions;
	}
}
