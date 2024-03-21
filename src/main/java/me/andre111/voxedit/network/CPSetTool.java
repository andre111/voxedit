package me.andre111.voxedit.network;

import io.netty.buffer.ByteBuf;
import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.tool.ConfiguredTool;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record CPSetTool(ConfiguredTool<?, ?> tool) implements CustomPayload {
	public static final Id<CPSetTool> ID = new Id<>(VoxEdit.id("set_tool"));
	public static final PacketCodec<ByteBuf, CPSetTool> CODEC = PacketCodecs.codec(ConfiguredTool.CODEC).xmap(CPSetTool::new, CPSetTool::tool);
	static {
		PayloadTypeRegistry.playC2S().register(ID, CODEC);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
