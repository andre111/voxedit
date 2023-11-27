package me.andre111.voxedit.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.andre111.voxedit.BlockPalette;
import me.andre111.voxedit.ToolState;
import me.andre111.voxedit.editor.Editor;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ToolBrush extends Tool {
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
		Editor.undoable(player, world, editable -> {
			for(BlockPos pos : getBlockPositions(world, target, state)) {
				editable.setBlock(pos, Blocks.AIR.getDefaultState());
			}
		});
	}

	@Override
	public List<ToolState> getCreativeMenuStates() {
		return List.of(
				ToolState.of(this),
				ToolState.of(this).withMode(ToolState.Mode.PAINT_TOP).withRadius(5).withBlockPalette(new BlockPalette(Util.make(new ArrayList<>(), list -> {
					list.add(new BlockPalette.Entry(Blocks.GRASS_BLOCK.getDefaultState(), 1));
					list.add(new BlockPalette.Entry(Blocks.MOSS_BLOCK.getDefaultState(), 3));
					list.add(new BlockPalette.Entry(Blocks.GREEN_CONCRETE_POWDER.getDefaultState(), 2));
				}))),
				ToolState.of(this).withMode(ToolState.Mode.SCATTER).withRadius(6).withBlockPalette(new BlockPalette(Util.make(new ArrayList<>(), list -> {
					list.add(new BlockPalette.Entry(Blocks.AIR.getDefaultState(), 20));
					list.add(new BlockPalette.Entry(Blocks.SHORT_GRASS.getDefaultState(), 5));
					list.add(new BlockPalette.Entry(Blocks.FERN.getDefaultState(), 5));
					list.add(new BlockPalette.Entry(Blocks.POPPY.getDefaultState(), 1));
					list.add(new BlockPalette.Entry(Blocks.DANDELION.getDefaultState(), 1));
				})))
		);
	}
}
