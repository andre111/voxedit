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

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.editor.EditStats;
import me.andre111.voxedit.editor.EditType;
import me.andre111.voxedit.editor.EditHistory;
import me.andre111.voxedit.editor.EditHistoryState;
import me.andre111.voxedit.item.VoxEditItem;
import me.andre111.voxedit.state.Schematic;
import me.andre111.voxedit.state.Selection;
import me.andre111.voxedit.state.ServerStates;
import me.andre111.voxedit.tool.ConfiguredTool;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public class ServerNetworking {
	private static Map<UUID, Consumer<NbtCompound>> nbtEditorTargets = new HashMap<>();
	
	public static void init() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			EditHistory history = EditHistory.of(handler.getPlayer(), handler.getPlayer().getServerWorld());
			List<EditStats> stats = history.getStates().stream().map(EditHistoryState::getStats).toList();
			int index = history.getIndex();
			
			sender.sendPacket(new CPHistoryInfo(stats, index, false));
		});
		
		ServerPlayNetworking.registerGlobalReceiver(CPCommand.ID, (payload, context) -> {
			if(!context.player().isCreative()) return;
			
			context.player().server.execute(() -> {
				ServerWorld world = (ServerWorld) context.player().getWorld();
				EditHistory history = EditHistory.of(context.player(), world);
				switch(payload.command()) {
				case UNDO:
					history.undo(world).inform(context.player(), EditType.UNDO);
					context.responseSender().sendPacket(new CPHistoryInfo(List.of(), history.getIndex(), true));
					break;
				case REDO:
					history.redo(world).inform(context.player(), EditType.REDO);
					context.responseSender().sendPacket(new CPHistoryInfo(List.of(), history.getIndex(), true));
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
						Schematic copy = Schematic.create(world.getRegistryManager(), world, sel.toBlockBox());
						ServerStates.get(context.player()).schematic(VoxEdit.id("copy_buffer"), copy, true);
					}
					break;
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
		
		ServerPlayNetworking.registerGlobalReceiver(CPAction.ID, (payload, context) -> {
			performAction(payload, context.player());
		});
	}
	
	private static void performAction(CPAction action, ServerPlayerEntity player) {
		if(!player.isCreative()) return;
		
		ConfiguredTool tool = action.tool();
		tool.tool().performAction(player, action.action(), action.targets(), action.context(), tool.config(), ServerStates.get(player));
	}
	
	public static void serverSendOpenNBTEditor(ServerPlayerEntity player, NbtCompound root, Consumer<NbtCompound> editTarget) {
		nbtEditorTargets.put(player.getUuid(), editTarget);
		ServerPlayNetworking.send(player, new CPNBTEditor(false, root));
	}
}
