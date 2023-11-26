package me.andre111.voxedit.tool;

import java.util.List;
import java.util.Set;

import me.andre111.voxedit.ToolState;
import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.editor.Editor;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ToolItemSmooth extends ToolItem {
	public ToolItemSmooth() {
		super(false, true, true, false);
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		if(!world.isClient) {
			ToolState state = readState(player.getStackInHand(hand));
			BlockPos target = VoxEdit.getTargetOf(player, state);
			if(state != null && target != null) {
				Set<BlockPos> positions = state.getBlockPositions(world, target);
				
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
				
				return TypedActionResult.success(player.getStackInHand(hand));
			}
			return TypedActionResult.fail(player.getStackInHand(hand));
		}
		return TypedActionResult.consume(player.getStackInHand(hand));
	}

	@Override
	public void leftClicked(World world, PlayerEntity player, Hand hand) {
	}
}
