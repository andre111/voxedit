package me.andre111.voxedit.editor.action;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

//TODO: store and restore blockentities!
//TODO: somehow make other blocks not update/drop as items?
public class SetBlockAction extends EditAction {
	private final BlockPos pos;
	private final BlockState oldState;
	private final BlockState newState;

	public SetBlockAction(World world, BlockPos pos, BlockState newState) {
		this.pos = pos;
		this.oldState = world.getBlockState(pos);
		this.newState = newState;
	}

	@Override
	public int undo(World world) {
		world.setBlockState(pos, oldState, Block.NOTIFY_LISTENERS | Block.SKIP_DROPS, 0);
		return 1;
	}
	
	@Override
	public int redo(World world) {
		world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS | Block.SKIP_DROPS, 0);
		return 1;
	}
}
