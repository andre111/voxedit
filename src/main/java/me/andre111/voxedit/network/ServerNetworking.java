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
package me.andre111.voxedit.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import me.andre111.voxedit.VoxEditUtil;
import me.andre111.voxedit.editor.EditStats;
import me.andre111.voxedit.editor.EditType;
import me.andre111.voxedit.editor.Editor;
import me.andre111.voxedit.editor.EditorWorld;
import me.andre111.voxedit.editor.FakeWorld;
import me.andre111.voxedit.editor.history.EditHistory;
import me.andre111.voxedit.editor.history.EditHistoryState;
import me.andre111.voxedit.schematic.Schematic;
import me.andre111.voxedit.state.ServerState;
import me.andre111.voxedit.state.ServerStates;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public class ServerNetworking {
	private static Map<UUID, Consumer<NbtCompound>> nbtEditorTargets = new HashMap<>();
	
	public static void init() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			// history
			EditHistory history = EditHistory.of(handler.getPlayer(), handler.getPlayer().getServerWorld());
			List<EditStats> stats = history.getStates().stream().map(EditHistoryState::getStats).toList();
			int index = history.getIndex();
			sender.sendPacket(new CPHistoryInfo(stats, index, false, history.getSize()));
			
			// schematics //TODO: improve to not send all data all the time
			for(var e : ServerStates.get(handler.getPlayer()).schematics().entrySet()) {
				NbtCompound nbt = new NbtCompound();
				e.getValue().writeNbt(server.getRegistryManager(), nbt);
				sender.sendPacket(new CPSchematic(e.getKey(), true, nbt));
			}
		});
		
		ServerPlayNetworking.registerGlobalReceiver(CPCommand.ID, (payload, context) -> {
			if(!context.player().isCreative()) return;
			
			context.player().server.execute(() -> {
				ServerWorld world = (ServerWorld) context.player().getWorld();
				ServerState state = ServerStates.get(context.player());
				EditHistory history = EditHistory.of(context.player(), world);
				switch(payload.command()) {
				case UNDO:
					history.undo(world).inform(context.player(), EditType.UNDO);
					context.responseSender().sendPacket(new CPHistoryInfo(List.of(), history.getIndex(), true, history.getSize()));
					break;
				case REDO:
					history.redo(world).inform(context.player(), EditType.REDO);
					context.responseSender().sendPacket(new CPHistoryInfo(List.of(), history.getIndex(), true, history.getSize()));
					break;
				case CLEAR_HISTORY:
					history.clear();
					context.responseSender().sendPacket(new CPHistoryInfo(List.of(), -1, false, history.getSize()));
					break;
				case EDITOR_ACTIVATE:
					state.editorActive(true);
					break;
				case EDITOR_DEACTIVATE:
					state.editorActive(false);
					break;
				case SAVE_SCHEMATIC:
					if(payload.data().isBlank()) return;
					if(state.selection() == null) return;
					Schematic schematic = Schematic.create(world.getRegistryManager(), world, state.selection());
					state.schematic(payload.data(), schematic, true, true);
					break;
				case DELETE_SCHEMATIC:
					if(payload.data().isBlank()) return;
					state.schematicDelete(payload.data());
					break;
				case DEV:
					break;
				case DEV_GET_FEATURE_CONFIG:
					var registry = context.player().getServerWorld().getRegistryManager().get(RegistryKeys.CONFIGURED_FEATURE);
					var feature = registry.get(Identifier.tryParse(payload.data()));
					if(feature != null) {
						String json = VoxEditUtil.toJson(feature, ConfiguredFeature.CODEC, context.player().getServerWorld().getRegistryManager());
						if(json != null) {
							context.responseSender().sendPacket(new CPCommand(Command.DEV_GET_FEATURE_CONFIG, json));
							break;
						}
					}
					context.responseSender().sendPacket(new CPCommand(Command.DEV_GET_FEATURE_CONFIG, ""));
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
		
		ServerPlayNetworking.registerGlobalReceiver(CPSelection.ID, (payload, context) -> {
			ServerStates.get(context.player()).selection(payload.selection());
		});
		
		ServerPlayNetworking.registerGlobalReceiver(CPPlaceSchematic.ID, (payload, context) -> {
			if(!context.player().isCreative()) return;
			
			context.player().server.execute(() -> {
				ServerWorld world = (ServerWorld) context.player().getWorld();
				ServerState state = ServerStates.get(context.player());
				Schematic schematic = state.schematic(payload.name());
				if(schematic == null) return;
				
				Editor.undoable(context.player(), world, Text.translatable("voxedit.schematic.name", payload.name()), (editable) -> {
					schematic.rotated(editable.getRegistryManager(), payload.rotation()).rotated(editable.getRegistryManager(), payload.yaw()).apply(editable, payload.pos());
				}, payload.pos(), false).inform(context.player(), EditType.PERFORM);
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(CPEntityMove.ID, (payload, context) -> {
			if(!context.player().isCreative()) return;
			
			context.player().server.execute(() -> {
				ServerWorld world = (ServerWorld) context.player().getWorld();
				Entity entity = world.getEntity(payload.uuid());
				if(entity == null) return;
				
				//TODO: make it undo-able, but combine consecutive actions
				entity.refreshPositionAndAngles(payload.pos().x, payload.pos().y, payload.pos().z, payload.yaw(), entity.getPitch());
				//TODO: update to all relevant players
				context.responseSender().sendPacket(new EntityPositionS2CPacket(entity));
			});
		});
		
		ServerPlayNetworking.registerGlobalReceiver(CPGenerateExample.ID, (payload, context) -> {
			if(!context.player().isCreative()) return;
			
			context.player().server.execute(() -> {
				ConfiguredFeature<?, ?> configuredFeature = VoxEditUtil.parseJson(payload.featureJson(), ConfiguredFeature.CODEC, null, context.player().getServerWorld().getRegistryManager());
				if(configuredFeature == null) return;
				
				ServerState state = ServerStates.get(context.player());

				boolean done = false;
				EditorWorld worldAccess = null;
				BlockPos sourcePos = null;
				
				DynamicRegistryManager drm = context.player().getServerWorld().getRegistryManager();
				ChunkGenerator chunkGenerator = new FlatChunkGenerator(new FlatChunkGeneratorConfig(Optional.empty(), drm.get(RegistryKeys.BIOME).getDefaultEntry().get(), List.of()));
				
				Schematic schematic = null;
				
				//TODO: it would probably be best to let the user define (and choose preset) world types
				BlockState[] surfaceStates = new BlockState[] { Blocks.GRASS_BLOCK.getDefaultState(), Blocks.DIRT.getDefaultState(), Blocks.WATER.getDefaultState(), Blocks.NETHERRACK.getDefaultState() };
				BlockPos[] startPositions = { context.player().getBlockPos().withY(64), context.player().getBlockPos().withY(65), context.player().getBlockPos().withY(40), context.player().getBlockPos().withY(32), context.player().getBlockPos().withY(80) };
				for(BlockState surfaceState : surfaceStates) {
					worldAccess = new EditorWorld(new FakeWorld(context.player().getServerWorld(), surfaceState));
					int startPosIndex = 0;
					sourcePos = startPositions[startPosIndex];
					while(sourcePos.getY() >= 0) {
						Random random = Random.create(payload.seed());
				        if(configuredFeature.generate(worldAccess, chunkGenerator, random, sourcePos)) {
				        	EditStats result = worldAccess.toSchematic(context.player(), Text.empty(), sourcePos);
							schematic = result.schematic();
							if(schematic != null) {
					        	done = true;
					        	break;
							}
				        }
				        if(++startPosIndex < startPositions.length) {
				        	sourcePos = startPositions[startPosIndex];
				        } else {
				        	sourcePos = sourcePos.down();
				        }
					}
					if(done) break;
				}
				
				if(schematic != null) {
					state.schematic("voxedit.example", schematic, true, false);
				} else {
					System.out.println("Failed generating example");
				}
			});
		});
	}
	
	private static void performAction(CPAction action, ServerPlayerEntity player) {
		if(!player.isCreative()) return;
		
		var tool = action.tool();
		tool.value().performAction(player, action.action(), action.targets(), action.context(), tool.config(), ServerStates.get(player));
	}
	
	public static void serverSendOpenNBTEditor(ServerPlayerEntity player, NbtCompound root, Consumer<NbtCompound> editTarget) {
		nbtEditorTargets.put(player.getUuid(), editTarget);
		ServerPlayNetworking.send(player, new CPNBTEditor(false, root));
	}
}
