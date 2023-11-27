package me.andre111.voxedit.tool;

import java.util.Set;

import me.andre111.voxedit.ToolState;
import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.editor.Editor;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ToolItemFlatten extends ToolItem {
	public ToolItemFlatten() {
		super(false, true, true, true, false);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		if(!world.isClient && player.isCreative()) {
			ToolState state = readState(player.getStackInHand(hand));
			BlockHitResult target = VoxEdit.getTargetOf(player, state);
			if(state != null && target != null) {
				Editor.undoable(player, world, editable -> {
					for(BlockPos pos : getBlockPositions(world, target, state)) {
						if(ToolState.isFree(world, pos)) {
							editable.setBlock(pos, state.palette().getRandom(world.getRandom()));
						} else {
							editable.setBlock(pos, Blocks.AIR.getDefaultState());
						}
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

	@Override
	public Set<BlockPos> getBlockPositions(World world, BlockHitResult target, ToolState state) {
		BlockPos center = target.getBlockPos();
		if(ToolState.isFree(world, center)) return Set.of();
		
		Set<BlockPos> positions = super.getBlockPositions(world, target, state);
		positions.removeIf(pos -> {
			int offset = switch(target.getSide()) {
			case UP -> pos.getY() - center.getY();
			case DOWN -> center.getY() - pos.getY();
			case SOUTH -> pos.getZ() - center.getZ();
			case NORTH -> center.getZ() - pos.getZ();
			case EAST -> pos.getX() - center.getX();
			case WEST -> center.getX() - pos.getX();
			};
			if(offset <= 0) return !ToolState.isFree(world, pos);
			else return ToolState.isFree(world, pos);
		});
		return positions;
	}
}
