package me.andre111.voxedit;

import java.util.UUID;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EditorItem extends Item {
	public EditorItem() {
		super(new Item.Settings().maxCount(1));
	}
	
	@Override
    public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
    	if(!world.isClient) {
    		BlockPos targetPos = context.getBlockPos();
    		BlockEntity entity = world.getBlockEntity(targetPos);
    		if(entity != null) {
    			Networking.serverSendOpenNBTEditor((ServerPlayerEntity) context.getPlayer(), entity.createNbt(), (nbt) -> {
    				entity.readNbt(nbt);
    				entity.markDirty();
	    		    //world.updateListeners(context.getBlockPos(), blockState, blockState, Block.NOTIFY_ALL);
    			});
    			return ActionResult.SUCCESS;
    		}
    		return ActionResult.FAIL;
    	}
		return ActionResult.CONSUME;
    }

	@Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
		if(!player.getWorld().isClient) {
			Networking.serverSendOpenNBTEditor((ServerPlayerEntity) player, entity.writeNbt(new NbtCompound()), (nbt) -> {
		        UUID uuid = entity.getUuid();
		        entity.readNbt(nbt);
		        entity.setUuid(uuid);
			});
			return ActionResult.SUCCESS;
		}
		return ActionResult.CONSUME;
    }
}
