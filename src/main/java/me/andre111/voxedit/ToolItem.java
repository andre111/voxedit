package me.andre111.voxedit;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtOps;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public final class ToolItem extends Item {
	public ToolItem() {
		super(new Item.Settings().maxCount(1));
	}
	
	@Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		if(!world.isClient && player.isCreative()) {
			ToolState state = readState(player.getStackInHand(hand));
			BlockHitResult target = VoxEdit.getTargetOf(player, state);
			if(state != null && target != null) {
				int count = state.tool().rightClick(world, player, target, state, state.tool().getBlockPositions(world, target, state));
				if(count > 0) player.sendMessage(Text.of("Set "+count+" blocks"), true);
				
				return TypedActionResult.success(player.getStackInHand(hand));
			}
			return TypedActionResult.fail(player.getStackInHand(hand));
		}
		return TypedActionResult.consume(player.getStackInHand(hand));
	}
	
	public void leftClicked(World world, PlayerEntity player, Hand hand) {
		if(!world.isClient && player.isCreative()) {
			ToolState state = readState(player.getStackInHand(hand));
			BlockHitResult target = VoxEdit.getTargetOf(player, state);
			if(state != null && target != null) {
				int count = state.tool().leftClick(world, player, target, state, state.tool().getBlockPositions(world, target, state));
				if(count > 0) player.sendMessage(Text.of("Set "+count+" blocks"), true);
			}
		}
	}
	
	@Override
	public ItemStack getDefaultStack() {
		return getStackWith(ToolState.of(VoxEdit.TOOL_BRUSH));
	}
	
	public ItemStack getStackWith(ToolState state) {
		ItemStack stack = super.getDefaultStack();
		storeState(stack, state);
		return stack;
	}
	
	public static ToolState readState(ItemStack stack) {
		return ToolState.CODEC.decode(NbtOps.INSTANCE, stack.getOrCreateSubNbt("voxedit:toolstate")).result().get().getFirst();
	}
	
	public static void storeState(ItemStack stack, ToolState state) {
		stack.setSubNbt("voxedit:toolstate", ToolState.CODEC.encodeStart(NbtOps.INSTANCE, state).result().get());
	}
}
