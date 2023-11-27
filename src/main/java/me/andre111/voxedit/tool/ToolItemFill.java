package me.andre111.voxedit.tool;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import me.andre111.voxedit.ToolState;
import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.editor.Editor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ToolItemFill extends ToolItem {
	public ToolItemFill() {
		super(false, false, true, true, true);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		if(!world.isClient && player.isCreative()) {
			ToolState state = readState(player.getStackInHand(hand));
			BlockHitResult target = VoxEdit.getTargetOf(player, state);
			if(state != null && target != null) {
				Editor.undoable(player, world, editable -> {
					for(BlockPos pos : getBlockPositions(world, target, state)) {
						editable.setBlock(pos, state.palette().getRandom(world.getRandom()));
					}
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


	public Set<BlockPos> getBlockPositions(World world, BlockPos center, ToolState state) {
		Set<BlockPos> positions = new HashSet<>();
		
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
}
