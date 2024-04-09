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
import me.andre111.voxedit.editor.Editor;
import me.andre111.voxedit.editor.Undo;
import me.andre111.voxedit.item.VoxEditItem;
import me.andre111.voxedit.network.CPAction.Action;
import me.andre111.voxedit.state.Schematic;
import me.andre111.voxedit.state.Selection;
import me.andre111.voxedit.state.ServerStates;
import me.andre111.voxedit.tool.ConfiguredTool;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
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
		
		// collect position sets
		if(action.targets().size() > 1 && !tool.tool().properties().draggable()) {
			player.sendMessage(Text.translatable("voxedit.feedback.notDraggable"), true);
			return;
		}
		List<Set<BlockPos>> positions = new ArrayList<>();
		for(int i=0; i<action.targets().size(); i++) {
			positions.add(tool.tool().getBlockPositions(player.getWorld(), action.targets().get(i), action.context(), tool.config()));
		}
		
		// run actions
		EditStats result = EditStats.EMPTY;
		if(action.action() == Action.PREVIEW) {
			result = Editor.undoable(player, player.getServerWorld(), (editable) -> {
				for(int i=0; i<action.targets().size(); i++) {
					if(positions.get(i).isEmpty()) continue;
					tool.tool().place(editable, player, action.targets().get(i), action.context(), tool.config(), positions.get(i));
				}
			}, action.targets().getFirst().pos(), true);
			
			ServerStates.get(player).schematic(VoxEdit.id("preview."+tool.tool().id().toTranslationKey()), result.schematic(), true);
		} else if(action.action() == Action.APPLY_PREVIEW) {
			Schematic preview = ServerStates.get(player).schematic(VoxEdit.id("preview."+action.tool().tool().id().toTranslationKey()));
			if(preview == null) {
				player.sendMessage(Text.translatable("voxedit.feedback.noPreview"), true);
				return;
			}
			if(action.targets().size() != 1) {
				player.sendMessage(Text.translatable("voxedit.feedback.previewSinglePosition"), true);
				return;
			}
			
			result = Editor.undoable(player, player.getServerWorld(), (editable) -> {
				preview.apply(editable, action.targets().getFirst().pos());
			}, action.targets().getFirst().pos(), false);
		} else {
			result = Editor.undoable(player, player.getServerWorld(), (editable) -> {
				for(int i=0; i<action.targets().size(); i++) {
					if(positions.get(i).isEmpty()) continue;
					if(action.action() == Action.PLACE) {
						tool.tool().place(editable, player, action.targets().get(i), action.context(), tool.config(), positions.get(i));
					} else if(action.action() == Action.REMOVE) {
						tool.tool().remove(editable, player, action.targets().get(i), action.context(), tool.config(), positions.get(i));
					}
				}
			}, action.targets().getFirst().pos(), false);
		}
		result.inform(player, EditType.PERFORM);
	}
	
	public static void serverSendOpenNBTEditor(ServerPlayerEntity player, NbtCompound root, Consumer<NbtCompound> editTarget) {
		nbtEditorTargets.put(player.getUuid(), editTarget);
		ServerPlayNetworking.send(player, new CPNBTEditor(false, root));
	}
}
