/*
 * Copyright (c) 2024 André Schweiger
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
package me.andre111.voxedit.tool;

import java.util.List;

import me.andre111.voxedit.editor.EditType;
import me.andre111.voxedit.editor.Editor;
import me.andre111.voxedit.editor.action.ModifyEntityAction;
import me.andre111.voxedit.network.ServerNetworking;
import me.andre111.voxedit.state.ServerState;
import me.andre111.voxedit.tool.data.Context;
import me.andre111.voxedit.tool.data.RaycastTargets;
import me.andre111.voxedit.tool.data.Target;
import me.andre111.voxedit.tool.data.ToolConfig;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class EditNBTTool extends Tool {

	public EditNBTTool() {
		super(Properties.NONE);
	}

	@Override
	public RaycastTargets getRaycastTargets(ToolConfig config) {
		return RaycastTargets.BLOCKS_AND_ENTITIES;
	}

	@Override
	public void performAction(ServerPlayerEntity player, Action action, List<Target> targets, Context context, ToolConfig config, ServerState state) {
		if(targets.size() != 1) return;
		Target target = targets.get(0);
		
		if(target.entity().isPresent()) {
			Entity entity = player.getServerWorld().getEntity(target.getEntity());
			if(entity == null) return;
			
			NbtCompound oldNbt = entity.writeNbt(new NbtCompound());
			ServerNetworking.serverSendOpenNBTEditor(player, oldNbt, (nbt) -> {
				if(nbt.equals(oldNbt)) return;
				
				Editor.undoableAction(player, player.getServerWorld(), asText(), new ModifyEntityAction(entity.getUuid(), oldNbt, nbt)).inform(player, EditType.PERFORM);
			});
		} else if(target.pos().isPresent()) {
			BlockPos targetPos = target.getBlockPos();
    		BlockEntity be = player.getServerWorld().getBlockEntity(targetPos);
    		if(be == null) return;
    		
    		NbtCompound oldNbt = be.createNbtWithId(player.getServerWorld().getRegistryManager());
			ServerNetworking.serverSendOpenNBTEditor(player, be.createNbt(player.getServerWorld().getRegistryManager()), (nbt) -> {
				if(nbt.equals(oldNbt)) return;
				
				Editor.undoable(player, player.getServerWorld(), asText(), (editable) -> {
					BlockEntity newBe = editable.getBlockEntity(targetPos);
					if(newBe != null) newBe.read(nbt, player.getServerWorld().getRegistryManager());
				}, null, false).inform(player, EditType.PERFORM);
			});
		}
	}

}
