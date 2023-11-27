package me.andre111.voxedit.tool;

import java.util.List;
import java.util.Set;

import me.andre111.voxedit.ToolState;
import me.andre111.voxedit.editor.Editor;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ToolSmooth extends Tool {
	@Override
	public void rightClick(World world, PlayerEntity player, BlockHitResult target, ToolState state, Set<BlockPos> positions) {
		Editor.undoable(player, world, editable -> {
			// erode
			positions.stream().filter(pos -> !world.isAir(pos)).forEach(pos -> {
				int neighborCount = (int) Direction.stream().map(pos::offset).filter(neighbor -> !world.isAir(neighbor)).count();
				if(neighborCount < 5) editable.setBlock(pos, Blocks.AIR.getDefaultState());
			});
			editable.applyCurrentChanges();
			
			// fill
			positions.stream().filter(pos -> world.isAir(pos)).forEach(pos -> {
				List<BlockPos> neighbors = Direction.stream().map(pos::offset).filter(neighbor -> !world.isAir(neighbor)).toList();
				if(neighbors.size() > 2) {
					editable.setBlock(pos, world.getBlockState(neighbors.get(world.getRandom().nextInt(neighbors.size()))));
				}
			});
		});
	}

	@Override
	public void leftClick(World world, PlayerEntity player, BlockHitResult target, ToolState state, Set<BlockPos> positions) {
	}
	
	@Override
	public boolean usesMode() {
		return false;
	}

	@Override
	public boolean usesBlockPalette() {
		return false;
	}
}
