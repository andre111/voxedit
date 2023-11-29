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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import com.mojang.datafixers.util.Pair;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.editor.Undo;
import me.andre111.voxedit.item.ToolItem;
import me.andre111.voxedit.item.VoxEditItem;
import me.andre111.voxedit.tool.ConfiguredTool;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ServerNetworking {
	private static Map<UUID, Consumer<NbtCompound>> nbtEditorTargets = new HashMap<>();
	
	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(VoxEdit.id("set_tool"), (server, player, handler, buf, responseSender) -> {
			if(!player.isCreative()) return;
			
    		NbtCompound tag = buf.readNbt();
    		ConfiguredTool<?, ?> tool = ConfiguredTool.CODEC.decode(NbtOps.INSTANCE, tag).result().get().getFirst();
    		ItemStack stack = player.getMainHandStack();
    		if(stack.getItem() instanceof ToolItem) {
    			ToolItem.storeToolData(stack, ToolItem.readToolData(stack).replaceSelected(tool));
    			
    			List<Pair<EquipmentSlot, ItemStack>> list = List.of(Pair.of(EquipmentSlot.MAINHAND, stack));
    			responseSender.sendPacket(new EntityEquipmentUpdateS2CPacket(player.getId(), list));
    		}
    	});
		
		ServerPlayNetworking.registerGlobalReceiver(VoxEdit.id("select_tool"), (server, player, handler, buf, responseSender) -> {
			if(!player.isCreative()) return;
			
			int index = buf.readInt();
    		ItemStack stack = player.getMainHandStack();
    		if(stack.getItem() instanceof ToolItem) {
    			ToolItem.storeToolData(stack, ToolItem.readToolData(stack).select(index));
    			
    			List<Pair<EquipmentSlot, ItemStack>> list = List.of(Pair.of(EquipmentSlot.MAINHAND, stack));
    			responseSender.sendPacket(new EntityEquipmentUpdateS2CPacket(player.getId(), list));
    		}
    	});
		
		ServerPlayNetworking.registerGlobalReceiver(VoxEdit.id("command"), (server, player, handler, buf, responseSender) -> {
			if(!player.isCreative()) return;
			
			World world = player.getWorld();
			Command command = Command.valueOf(buf.readString());
			switch(command) {
			case UNDO:
				int undoCount = Undo.of(player, world).undo(world);
				if(undoCount > 0) player.sendMessage(Text.of("Undone "+undoCount+" block changes."), true);
				break;
			case REDO:
				int redoCount = Undo.of(player, world).redo(world);
				if(redoCount > 0) player.sendMessage(Text.of("Redone "+redoCount+" block changes."), true);
				break;
			case LEFT_CLICK:
				ItemStack stack = player.getMainHandStack();
	    		if(stack.getItem() instanceof VoxEditItem item) {
					//TODO: verify attack cooldown
					item.leftClicked(world, player, Hand.MAIN_HAND);
				}
				break;
			}
		});
		
		ServerPlayNetworking.registerGlobalReceiver(VoxEdit.id("nbteditor"), (server, player, handler, buf, responseSender) -> {
			if(!player.isCreative()) return;
			if(!nbtEditorTargets.containsKey(player.getUuid())) return;
			if(buf.readBoolean()) {
				nbtEditorTargets.get(player.getUuid()).accept(buf.readNbt());
			}
			nbtEditorTargets.remove(player.getUuid());
		});

		ServerPlayNetworking.registerGlobalReceiver(VoxEdit.id("request_registry_ids"), (server, player, handler, buf, responseSender) -> {
			if(!player.isCreative()) return;
			
			int id = buf.readInt();
			RegistryKey<? extends Registry<?>> registryKey = buf.readRegistryRefKey();
			var registry = server.getRegistryManager().getOptional(registryKey);

			PacketByteBuf responseBuf = PacketByteBufs.create();
			responseBuf.writeInt(id);
			if(registry.isPresent()) {
				Set<Identifier> registryIDs = registry.get().getIds();
				responseBuf.writeInt(registryIDs.size());
				for(Identifier registryID : registryIDs) responseBuf.writeIdentifier(registryID);
			} else {
				responseBuf.writeInt(0);
			}
			ServerPlayNetworking.send(player, VoxEdit.id("registry_ids"), responseBuf);
		});
	}
	
	public static void serverSendOpenNBTEditor(ServerPlayerEntity player, NbtCompound root, Consumer<NbtCompound> editTarget) {
		nbtEditorTargets.put(player.getUuid(), editTarget);
		
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeNbt(root);
		ServerPlayNetworking.send(player, VoxEdit.id("nbteditor"), buf);
	}
}
