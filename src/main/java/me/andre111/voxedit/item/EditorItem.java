/*
 * Copyright (c) 2023 André Schweiger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.andre111.voxedit.item;

import java.util.UUID;

import me.andre111.voxedit.network.ServerNetworking;
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

public class EditorItem extends Item implements VoxEditItem {
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
    			ServerNetworking.serverSendOpenNBTEditor((ServerPlayerEntity) context.getPlayer(), entity.createNbt(), (nbt) -> {
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
			ServerNetworking.serverSendOpenNBTEditor((ServerPlayerEntity) player, entity.writeNbt(new NbtCompound()), (nbt) -> {
		        UUID uuid = entity.getUuid();
		        entity.readNbt(nbt);
		        entity.setUuid(uuid);
			});
			return ActionResult.SUCCESS;
		}
		return ActionResult.CONSUME;
    }
	
	@Override
	public boolean useCustomControls() {
		return false;
	}
}
