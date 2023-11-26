package me.andre111.voxedit.tool;

import me.andre111.voxedit.ToolState;
import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.editor.Editor;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ToolItem extends Item {

	public ToolItem(Settings settings) {
		super(settings);
	}
	
	@Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		if(!world.isClient) {
			ToolState state = readState(player.getStackInHand(hand));
			BlockPos target = VoxEdit.getTargetOf(player, state);
			if(state != null && target != null) {
				Editor.undoable(player, world, editable -> {
					for(BlockPos pos : state.getBlockPositions(world, target)) {
						editable.setBlock(pos, state.blockState);
					}
				});
				
				return TypedActionResult.success(player.getStackInHand(hand));
			}
			return TypedActionResult.fail(player.getStackInHand(hand));
		}
		return TypedActionResult.consume(player.getStackInHand(hand));
    }
	
	public void leftClicked(World world, PlayerEntity player, Hand hand) {
		if(!world.isClient) {
			ToolState state = readState(player.getStackInHand(hand));
			BlockPos target = VoxEdit.getTargetOf(player, state);
			if(state != null && target != null) {
				Editor.undoable(player, world, editable -> {
					for(BlockPos pos : state.getBlockPositions(world, target)) {
						editable.setBlock(pos, Blocks.AIR.getDefaultState());
					}
				});
			}
		}
	}
	
	@Override
	public ItemStack getDefaultStack() {
		ItemStack stack = super.getDefaultStack();
		storeState(stack, new ToolState());
		return stack;
	}
	
	public ToolState readState(ItemStack stack) {
		return ToolState.CODEC.decode(NbtOps.INSTANCE, stack.getOrCreateSubNbt("voxedit:toolstate")).result().get().getFirst();
	}
	
	public void storeState(ItemStack stack, ToolState state) {
		stack.setSubNbt("voxedit:toolstate", ToolState.CODEC.encodeStart(NbtOps.INSTANCE, state).result().get());
	}
}
