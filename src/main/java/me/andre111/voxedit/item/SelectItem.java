package me.andre111.voxedit.item;

import me.andre111.voxedit.state.Selection;
import me.andre111.voxedit.state.ServerStates;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class SelectItem extends Item implements VoxEditItem {
	public SelectItem() {
		super(new Item.Settings().maxCount(1));
	}
	
	@Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		if(!world.isClient && player instanceof ServerPlayerEntity serverPlayer && player.isCreative()) {
			HitResult result = serverPlayer.raycast(64, 0, false);
			if(result instanceof BlockHitResult blockHit && !world.isAir(blockHit.getBlockPos())) {
				Selection prevSel = ServerStates.get(serverPlayer).getSelection();
				if(prevSel == null) ServerStates.get(serverPlayer).setSelection(new Selection(blockHit.getBlockPos(), blockHit.getBlockPos()));
				else ServerStates.get(serverPlayer).setSelection(prevSel.expandToInclude(blockHit.getBlockPos()));
				return TypedActionResult.success(player.getStackInHand(hand));
			}
			return TypedActionResult.fail(player.getStackInHand(hand));
		}
		return TypedActionResult.consume(player.getStackInHand(hand));
	}
	
	@Override
	public void leftClicked(World world, PlayerEntity player, Hand hand) {
		if(!world.isClient && player instanceof ServerPlayerEntity serverPlayer && player.isCreative()) {
			ServerStates.get(serverPlayer).setSelection(null);
		}
	}
}
