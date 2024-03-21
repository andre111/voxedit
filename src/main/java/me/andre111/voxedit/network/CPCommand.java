package me.andre111.voxedit.network;

import io.netty.buffer.ByteBuf;
import me.andre111.voxedit.VoxEdit;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record CPCommand(Command command) implements CustomPayload {
	public static final Id<CPCommand> ID = new Id<>(VoxEdit.id("command"));
	public static final PacketCodec<ByteBuf, CPCommand> CODEC = PacketCodecs.BYTE.xmap(b -> new CPCommand(Command.values()[b]), c -> (byte) c.command().ordinal());
	static {
		PayloadTypeRegistry.playC2S().register(ID, CODEC);
	}
	
	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
