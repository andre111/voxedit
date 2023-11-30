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

import me.andre111.voxedit.editor.EditType;
import me.andre111.voxedit.editor.Editor;
import me.andre111.voxedit.editor.action.ModifyEntityAction;
import me.andre111.voxedit.network.ServerNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class EditorItem extends Item implements VoxEditItem {
	public EditorItem() {
		super(new Item.Settings().maxCount(1));
	}
	
	@Override
    public ActionResult useOnBlock(ItemUsageContext context) {
    	if(!context.getWorld().isClient && context.getWorld() instanceof ServerWorld world) {
    		BlockPos targetPos = context.getBlockPos();
    		BlockEntity entity = world.getBlockEntity(targetPos);
    		if(entity != null) {
    			NbtCompound oldNbt = entity.createNbtWithId();
    			ServerNetworking.serverSendOpenNBTEditor((ServerPlayerEntity) context.getPlayer(), entity.createNbt(), (nbt) -> {
    				if(nbt.equals(oldNbt)) return;
    				
    				Editor.undoable(context.getPlayer(), world, (editable) -> {
    					BlockEntity newBe = editable.getBlockEntity(targetPos);
    					if(newBe != null) newBe.readNbt(nbt);
    				}).inform(context.getPlayer(), EditType.PERFORM);
    			});
    			return ActionResult.SUCCESS;
    		}
    		return ActionResult.FAIL;
    	}
		return ActionResult.CONSUME;
    }

	@Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
		if(!entity.getWorld().isClient && entity.getWorld() instanceof ServerWorld world) {
			NbtCompound oldNbt = entity.writeNbt(new NbtCompound());
			ServerNetworking.serverSendOpenNBTEditor((ServerPlayerEntity) player, oldNbt, (nbt) -> {
				if(nbt.equals(oldNbt)) return;
				
				Editor.undoableAction(player, world, new ModifyEntityAction(entity.getId(), oldNbt, nbt)).inform(player, EditType.PERFORM);
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
