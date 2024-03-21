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

import me.andre111.voxedit.client.ClientState;
import me.andre111.voxedit.client.gui.screen.NBTEditorScreen;
import me.andre111.voxedit.network.CPCommand;
import me.andre111.voxedit.network.CPNBTEditor;
import me.andre111.voxedit.network.CPRegistryList;
import me.andre111.voxedit.network.CPRequestRegistry;
import me.andre111.voxedit.network.CPSelectTool;
import me.andre111.voxedit.network.CPSetTool;
import me.andre111.voxedit.network.Command;
import me.andre111.voxedit.tool.ConfiguredTool;
import me.andre111.voxedit.tool.config.ToolConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

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

		/*
		ClientPlayNetworking.registerGlobalReceiver(VoxEdit.id("state"), (client, handler, buf, responseSender) -> {
			ClientState.INSTANCE.read(buf);
		});
		*/
	}
	

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void setSelectedConfig(ToolConfig<?> toolConfig) {
		if(ClientState.tool() == null) return;
		if(!ClientState.config().getClass().isAssignableFrom(toolConfig.getClass())) return;
		ClientNetworking.setTool(new ConfiguredTool(ClientState.tool(), toolConfig));
	}
	
	public static void setTool(ConfiguredTool<?, ?> tool) {
		ClientPlayNetworking.send(new CPSetTool(tool));
	}
	
	public static void selectTool(int index) {
		ClientPlayNetworking.send(new CPSelectTool(index));
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
