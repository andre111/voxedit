package me.andre111.voxedit.tool;

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

public class ToolItemBrush extends ToolItem {
	public ToolItemBrush() {
		super(true, true, true, true, true);
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
		if(!world.isClient && player.isCreative()) {
			ToolState state = readState(player.getStackInHand(hand));
			BlockHitResult target = VoxEdit.getTargetOf(player, state);
			if(state != null && target != null) {
				Editor.undoable(player, world, editable -> {
					for(BlockPos pos : getBlockPositions(world, target, state)) {
						editable.setBlock(pos, Blocks.AIR.getDefaultState());
					}
				});
			}
		}
	}
}
