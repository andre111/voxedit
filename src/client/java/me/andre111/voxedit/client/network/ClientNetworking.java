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
package me.andre111.voxedit.client.network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.client.ClientStates;
import me.andre111.voxedit.client.EditorState;
import me.andre111.voxedit.client.VoxEditClient;
import me.andre111.voxedit.client.gui.screen.NBTEditorScreen;
import me.andre111.voxedit.client.renderer.SchematicRenderer;
import me.andre111.voxedit.client.renderer.SchematicView;
import me.andre111.voxedit.network.CPClearSelection;
import me.andre111.voxedit.network.CPCommand;
import me.andre111.voxedit.network.CPNBTEditor;
import me.andre111.voxedit.network.CPRegistryList;
import me.andre111.voxedit.network.CPRequestRegistry;
import me.andre111.voxedit.network.CPSchematic;
import me.andre111.voxedit.network.CPSetSelection;
import me.andre111.voxedit.network.Command;
import me.andre111.voxedit.state.Schematic;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Environment(value=EnvType.CLIENT)
public class ClientNetworking {
	private static int nextRegistryRequestID = 0;
	private static Map<Integer, CompletableFuture<List<Identifier>>> registryRequests = new HashMap<>();
	
	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(CPNBTEditor.ID, (payload, context) -> {
			context.client().execute(() -> context.client().setScreen(new NBTEditorScreen(payload.nbt())));
		});
		
		ClientPlayNetworking.registerGlobalReceiver(CPRegistryList.ID, (payload, context) -> {
			CompletableFuture<List<Identifier>> request = registryRequests.remove(payload.requestID());
			if(request != null) {
				context.client().execute(() -> request.complete(payload.ids()));
			}
		});
		
		ClientPlayNetworking.registerGlobalReceiver(CPClearSelection.ID, (payload, context) -> {
			ClientStates.instance().setSelection(null, false);
		});
		
		ClientPlayNetworking.registerGlobalReceiver(CPSetSelection.ID, (payload, context) -> {
			ClientStates.instance().setSelection(payload.selection(), false);
		});
		
		ClientPlayNetworking.registerGlobalReceiver(CPSchematic.ID, (payload, context) -> {
			NbtCompound nbt = payload.nbt();
			Schematic schematic = nbt.isEmpty() ? null : Schematic.readNbt(context.client().world.getRegistryManager(), nbt);
			
			EditorState.schematic(payload.id(), schematic);
			if(payload.id().equals(VoxEdit.id("copy_buffer"))) {
				VoxEditClient.testRenderer = new SchematicRenderer(new SchematicView(context.client().world, new BlockPos(0, 0, 0), schematic));
			}
		});
	}
	
	public static void sendCommand(Command command) {
		ClientPlayNetworking.send(new CPCommand(command));
	}
	
	public static void sendNBTEditorResult(NbtCompound compound) {
		if(compound != null) ClientPlayNetworking.send(new CPNBTEditor(true, compound));
		else ClientPlayNetworking.send(new CPNBTEditor(false, new NbtCompound()));
	}
	
	public static CompletableFuture<List<Identifier>> getServerRegistryIDs(RegistryKey<? extends Registry<?>> registryKey) {
		int id = nextRegistryRequestID;
		CompletableFuture<List<Identifier>> future = new CompletableFuture<>();
		registryRequests.put(id, future);
		
		ClientPlayNetworking.send(new CPRequestRegistry(id, registryKey));
		
		return future;
	}
}
