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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.client.ClientState;
import me.andre111.voxedit.client.gui.screen.NBTEditorScreen;
import me.andre111.voxedit.network.Command;
import me.andre111.voxedit.tool.ConfiguredTool;
import me.andre111.voxedit.tool.config.ToolConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class ClientNetworking {
	private static int nextRegistryRequestID = 0;
	private static Map<Integer, CompletableFuture<List<Identifier>>> registryRequests = new HashMap<>();
	
	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(VoxEdit.id("nbteditor"), (client, handler, buf, responseSender) -> {
			client.execute(() -> client.setScreen(new NBTEditorScreen(buf.readNbt())));
		});
		
		ClientPlayNetworking.registerGlobalReceiver(VoxEdit.id("registry_ids"), (client, handler, buf, responseSender) -> {
			CompletableFuture<List<Identifier>> request = registryRequests.remove(buf.readInt());
			if(request != null) {
				int count = buf.readInt();
				List<Identifier> entries  = new ArrayList<>();
				for(int i=0; i<count; i++) entries.add(buf.readIdentifier());
				request.complete(entries);
			}
		});
	}
	

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void setSelectedConfig(ToolConfig<?> toolConfig) {
		if(ClientState.active == null) return;
		if(!ClientState.active.selected().config().getClass().isAssignableFrom(toolConfig.getClass())) return;
		ClientNetworking.setTool(new ConfiguredTool(ClientState.active.selected().tool(), toolConfig));
	}
	
	public static void setTool(ConfiguredTool<?, ?> tool) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeNbt(ConfiguredTool.CODEC.encodeStart(NbtOps.INSTANCE, tool).result().get());
		ClientPlayNetworking.send(VoxEdit.id("set_tool"), buf);
	}
	
	public static void selectTool(int index) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeInt(index);
		ClientPlayNetworking.send(VoxEdit.id("select_tool"), buf);
	}
	
	public static void sendCommand(Command command) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeString(command.name());
		ClientPlayNetworking.send(VoxEdit.id("command"), buf);
	}
	
	public static void sendNBTEditorResult(NbtCompound compound) {
		PacketByteBuf buf = PacketByteBufs.create();
		if(compound == null) {
			buf.writeBoolean(false);
		} else {
			buf.writeBoolean(true);
			buf.writeNbt(compound);
		}
		ClientPlayNetworking.send(VoxEdit.id("nbteditor"), buf);
	}
	
	public static CompletableFuture<List<Identifier>> getServerRegistryIDs(RegistryKey<? extends Registry<?>> registryKey) {
		int id = nextRegistryRequestID;
		CompletableFuture<List<Identifier>> future = new CompletableFuture<>();
		registryRequests.put(id, future);
		
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeInt(id);
		buf.writeRegistryKey(registryKey);
		ClientPlayNetworking.send(VoxEdit.id("request_registry_ids"), buf);
		
		return future;
	}
}
