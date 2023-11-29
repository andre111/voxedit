package me.andre111.voxedit.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.andre111.voxedit.BlockPalette;
import me.andre111.voxedit.editor.UndoRecordingStructureWorldAccess;
import me.andre111.voxedit.tool.config.ToolConfigBrush;
import me.andre111.voxedit.tool.util.Defaults;
import me.andre111.voxedit.tool.util.Mode;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class ToolBrush extends Tool<ToolConfigBrush, ToolBrush> {
	public ToolBrush() {
		super(ToolConfigBrush.CODEC, new ToolConfigBrush());
	}

	@Override
	public void rightClick(UndoRecordingStructureWorldAccess world, PlayerEntity player, BlockHitResult target, ToolConfigBrush config, Set<BlockPos> positions) {
		for(BlockPos pos : positions) {
			BlockState state = config.palette().getRandom(world.getRandom());
			if(!config.checkCanPlace() || state.canPlaceAt(world, pos)) world.setBlockState(pos, state, 0);
		}
	}

	@Override
	public void leftClick(UndoRecordingStructureWorldAccess world, PlayerEntity player, BlockHitResult target, ToolConfigBrush config, Set<BlockPos> positions) {
		for(BlockPos pos : positions) {
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 0);
		}
	}

	@Override
	public Set<BlockPos> getBlockPositions(BlockView world, BlockHitResult target, ToolConfigBrush config) {
		return Defaults.getBlockPositions(world, target, config.radius(), config.shape(), config.mode().testPredicate, config.filter());
	}

	public List<ToolConfigBrush> getAdditionalCreativeMenuConfigs() {
		return List.of(
				getDefaultConfig().withMode(Mode.PAINT_TOP).withRadius(5).withPalette(new BlockPalette(Util.make(new ArrayList<>(), list -> {
					list.add(new BlockPalette.Entry(Blocks.DIRT.getDefaultState(), 1));
					list.add(new BlockPalette.Entry(Blocks.GRAVEL.getDefaultState(), 1));
					list.add(new BlockPalette.Entry(Blocks.DIRT_PATH.getDefaultState(), 1));
				}))),
				getDefaultConfig().withMode(Mode.SCATTER).withRadius(6).withPalette(new BlockPalette(Util.make(new ArrayList<>(), list -> {
					list.add(new BlockPalette.Entry(Blocks.AIR.getDefaultState(), 20));
					list.add(new BlockPalette.Entry(Blocks.SHORT_GRASS.getDefaultState(), 5));
					list.add(new BlockPalette.Entry(Blocks.FERN.getDefaultState(), 5));
					list.add(new BlockPalette.Entry(Blocks.POPPY.getDefaultState(), 1));
					list.add(new BlockPalette.Entry(Blocks.DANDELION.getDefaultState(), 1));
				})))
				);
	}
}
