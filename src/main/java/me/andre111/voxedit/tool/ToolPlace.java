package me.andre111.voxedit.tool;

import java.util.Set;

import me.andre111.voxedit.editor.UndoRecordingStructureWorldAccess;
import me.andre111.voxedit.tool.config.ToolConfigPlace;
import me.andre111.voxedit.tool.util.Defaults;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public class ToolPlace extends Tool<ToolConfigPlace, ToolPlace> {
	public ToolPlace() {
		super(ToolConfigPlace.CODEC, new ToolConfigPlace());
	}

	@Override
	public void rightClick(UndoRecordingStructureWorldAccess world, PlayerEntity player, BlockHitResult target, ToolConfigPlace config, Set<BlockPos> positions) {
		ConfiguredFeature<?, ?> configuredFeature = world.getRegistryManager().get(RegistryKeys.CONFIGURED_FEATURE).get(config.feature());
		if(configuredFeature == null) return;
		
		BlockPos pos = positions.iterator().next();
		for(int t=0; t<config.tries(); t++) {
	        if(configuredFeature.generate(world, world.getChunkManager().getChunkGenerator(), world.getRandom(), pos)) break;
		}
	}

	@Override
	public void leftClick(UndoRecordingStructureWorldAccess world, PlayerEntity player, BlockHitResult target, ToolConfigPlace config, Set<BlockPos> positions) {
	}

	@Override
	public Set<BlockPos> getBlockPositions(BlockView world, BlockHitResult target, ToolConfigPlace config) {
		BlockPos targetPos = target.getBlockPos();
		if(Defaults.isFree(world, targetPos)) return Set.of();
		BlockPos up = targetPos.offset(target.getSide());
		if(!Defaults.isFree(world, up)) return Set.of();
		return Set.of(up);
	}
}
