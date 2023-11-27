package me.andre111.voxedit;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.datafixers.util.Pair;

import me.andre111.voxedit.editor.Undo;
import me.andre111.voxedit.gui.screen.NBTEditorScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class Networking {
	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(new Identifier("voxedit:set_tool_state"), (server, player, handler, buf, responseSender) -> {
			if(!player.isCreative()) return;
			
    		NbtCompound tag = buf.readNbt();
    		ToolState state = ToolState.CODEC.decode(NbtOps.INSTANCE, tag).result().get().getFirst();
    		ItemStack stack = player.getMainHandStack();
    		if(stack.getItem() instanceof ToolItem) {
    			ToolItem.storeState(stack, state);
    			
    			List<Pair<EquipmentSlot, ItemStack>> list = List.of(Pair.of(EquipmentSlot.MAINHAND, stack));
    			responseSender.sendPacket(new EntityEquipmentUpdateS2CPacket(player.getId(), list));
    		}
    	});
		ServerPlayNetworking.registerGlobalReceiver(new Identifier("voxedit:command"), (server, player, handler, buf, responseSender) -> {
			if(!player.isCreative()) return;
			
			World world = player.getWorld();
			String command = buf.readString();
			switch(command) {
			case "UNDO":
				int undoCount = Undo.of(player, world).undo(world);
				if(undoCount > 0) player.sendMessage(Text.of("Undone "+undoCount+" block changes."), true);
				break;
			case "REDO":
				int redoCount = Undo.of(player, world).redo(world);
				if(redoCount > 0) player.sendMessage(Text.of("Redone "+redoCount+" block changes."), true);
				break;
			case "TOOL_LEFT_CLICK":
				ItemStack stack = player.getMainHandStack();
	    		if(stack.getItem() instanceof ToolItem item) {
					//TODO: verify attack cooldown
					item.leftClicked(world, player, Hand.MAIN_HAND);
				}
				break;
			}
		});
		ServerPlayNetworking.registerGlobalReceiver(new Identifier("voxedit:nbteditor"), (server, player, handler, buf, responseSender) -> {
			if(!player.isCreative()) return;
			if(!ServerState.NBTEDITOR_TARGETS.containsKey(player.getUuid())) return;
			if(buf.readBoolean()) {
				ServerState.NBTEDITOR_TARGETS.get(player.getUuid()).accept(buf.readNbt());
			}
			ServerState.NBTEDITOR_TARGETS.remove(player.getUuid());
		});
		
		
		ClientPlayNetworking.registerGlobalReceiver(new Identifier("voxedit:feedback"), (client, handler, buf, responseSender) -> {
			
		});
		
		ClientPlayNetworking.registerGlobalReceiver(new Identifier("voxedit:nbteditor"), (client, handler, buf, responseSender) -> {
			client.execute(() -> client.setScreen(new NBTEditorScreen(buf.readNbt())));
		});
	}
	
	public static void clientSendToolState(ToolState state) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeNbt(ToolState.CODEC.encodeStart(NbtOps.INSTANCE, state).result().get());
		ClientPlayNetworking.send(new Identifier("voxedit:set_tool_state"), buf);
	}
	
	public static void clientSendCommand(Command command) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeString(command.name());
		ClientPlayNetworking.send(new Identifier("voxedit:command"), buf);
	}
	
	public static void clientSendNBTEditorResult(NbtCompound compound) {
		PacketByteBuf buf = PacketByteBufs.create();
		if(compound == null) {
			buf.writeBoolean(false);
		} else {
			buf.writeBoolean(true);
			buf.writeNbt(compound);
		}
		ClientPlayNetworking.send(new Identifier("voxedit:nbteditor"), buf);
	}
	
	public static void serverSendOpenNBTEditor(ServerPlayerEntity player, NbtCompound root, Consumer<NbtCompound> editTarget) {
		ServerState.NBTEDITOR_TARGETS.put(player.getUuid(), editTarget);
		
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeNbt(root);
		ServerPlayNetworking.send(player, new Identifier("voxedit:nbteditor"), buf);
	}
	
	public static enum Command {
		UNDO,
		REDO,
		TOOL_LEFT_CLICK;
	}
}
