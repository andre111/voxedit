package me.andre111.voxedit.network;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import me.andre111.voxedit.VoxEdit;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record CPClearSelection() implements CustomPayload {
	public static final Id<CPClearSelection> ID = new Id<>(VoxEdit.id("clear_selection"));
	public static final PacketCodec<ByteBuf, CPClearSelection> CODEC = PacketCodecs.codec(Codec.unit(new CPClearSelection()));
	static {
		PayloadTypeRegistry.playC2S().register(ID, CODEC);
		PayloadTypeRegistry.playS2C().register(ID, CODEC);
	}
	
	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
