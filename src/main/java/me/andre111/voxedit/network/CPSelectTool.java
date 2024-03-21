package me.andre111.voxedit.network;

import io.netty.buffer.ByteBuf;
import me.andre111.voxedit.VoxEdit;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record CPSelectTool(int index) implements CustomPayload {
	public static final Id<CPSelectTool> ID = new Id<>(VoxEdit.id("select_tool"));
	public static final PacketCodec<ByteBuf, CPSelectTool> CODEC = PacketCodecs.INTEGER.xmap(CPSelectTool::new, CPSelectTool::index);
	static {
		PayloadTypeRegistry.playC2S().register(ID, CODEC);
	}
	
	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
