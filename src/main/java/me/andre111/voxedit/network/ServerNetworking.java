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
package me.andre111.voxedit.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import com.mojang.datafixers.util.Pair;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.editor.EditType;
import me.andre111.voxedit.editor.Undo;
import me.andre111.voxedit.item.ToolItem;
import me.andre111.voxedit.item.ToolItem.Data;
import me.andre111.voxedit.item.VoxEditItem;
import me.andre111.voxedit.state.Selection;
import me.andre111.voxedit.state.ServerStates;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ServerNetworking {
	private static Map<UUID, Consumer<NbtCompound>> nbtEditorTargets = new HashMap<>();
	
	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(CPCommand.ID, (payload, context) -> {
			if(!context.player().isCreative()) return;
			
			context.player().server.execute(() -> {
				World world = context.player().getWorld();
				switch(payload.command()) {
				case UNDO:
					Undo.of(context.player(), world).undo(world).inform(context.player(), EditType.UNDO);
					break;
				case REDO:
					Undo.of(context.player(), world).redo(world).inform(context.player(), EditType.REDO);
					break;
				case LEFT_CLICK:
					ItemStack stack = context.player().getMainHandStack();
		    		if(stack.getItem() instanceof VoxEditItem item) {
						//TODO: verify attack cooldown
						item.leftClicked(world, context.player(), Hand.MAIN_HAND);
					}
					break;
				case DEV:
					Selection sel = ServerStates.get(context.player()).getSelection();
					if(sel != null) {
						StructureTemplate copy = new StructureTemplate();
						copy.saveFromWorld(world, sel.min(), sel.max().subtract(sel.min()).add(1, 1, 1), false, Blocks.STRUCTURE_VOID);
						ServerStates.get(context.player()).setCopyBuffer(copy);
					}
					break;
				}
			});
		});
		
		ServerPlayNetworking.registerGlobalReceiver(CPSetTool.ID, (payload, context) -> {
			if(!context.player().isCreative()) return;
    		
			context.player().server.execute(() -> {
	    		ItemStack stack = context.player().getMainHandStack();
	    		if(stack.getItem() instanceof ToolItem) {
	    			Data data = stack.get(VoxEdit.DATA_COMPONENT);
	    			if(data == null) data = new Data(payload.tool());
	    			else data.replaceSelected(payload.tool());
	    			stack.set(VoxEdit.DATA_COMPONENT, data);
	    			
	    			List<Pair<EquipmentSlot, ItemStack>> list = List.of(Pair.of(EquipmentSlot.MAINHAND, stack));
	    			context.responseSender().sendPacket(new EntityEquipmentUpdateS2CPacket(context.player().getId(), list));
	    		}
			});
    	});
		
		ServerPlayNetworking.registerGlobalReceiver(CPSelectTool.ID, (payload, context) -> {
			if(!context.player().isCreative()) return;

			context.player().server.execute(() -> {
	    		ItemStack stack = context.player().getMainHandStack();
	    		if(stack.getItem() instanceof ToolItem) {
	    			Data data = stack.get(VoxEdit.DATA_COMPONENT);
	    			data.select(payload.index());
	    			stack.set(VoxEdit.DATA_COMPONENT, data);

	    			List<Pair<EquipmentSlot, ItemStack>> list = List.of(Pair.of(EquipmentSlot.MAINHAND, stack));
	    			context.responseSender().sendPacket(new EntityEquipmentUpdateS2CPacket(context.player().getId(), list));
	    		}
			});
    	});
		
		ServerPlayNetworking.registerGlobalReceiver(CPNBTEditor.ID, (payload, context) -> {
			if(!context.player().isCreative()) return;
			if(!nbtEditorTargets.containsKey(context.player().getUuid())) return;
			
			if(!payload.apply()) {
				nbtEditorTargets.remove(context.player().getUuid());
				return;
			}

			context.player().server.execute(() -> {
				nbtEditorTargets.get(context.player().getUuid()).accept(payload.nbt());
				nbtEditorTargets.remove(context.player().getUuid());
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(CPRequestRegistry.ID, (payload, context) -> {
			if(!context.player().isCreative()) return;

			context.player().server.execute(() -> {
				var registry = context.player().server.getRegistryManager().getOptional(payload.registryKey());
				
				if(registry.isPresent()) {
					Set<Identifier> registryIDs = registry.get().getIds();
					ServerPlayNetworking.send(context.player(), new CPRegistryList(payload.requestID(), new ArrayList<>(registryIDs)));
				} else {
					ServerPlayNetworking.send(context.player(), new CPRegistryList(payload.requestID(), List.of()));
				}
			});
		});
		
		/*
		ServerPlayNetworking.registerGlobalReceiver(VoxEdit.id("state"), (server, player, handler, buf, responseSender) -> {
			if(!player.isCreative()) return;
			
			ServerStates.get(player).read(buf);
		});
		*/
	}
	
	public static void serverSendOpenNBTEditor(ServerPlayerEntity player, NbtCompound root, Consumer<NbtCompound> editTarget) {
		nbtEditorTargets.put(player.getUuid(), editTarget);
		ServerPlayNetworking.send(player, new CPNBTEditor(false, root));
	}
}
