package me.andre111.voxedit.network;

import java.util.List;

import io.netty.buffer.ByteBuf;
import me.andre111.voxedit.VoxEdit;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CPRegistryList(int requestID, List<Identifier> ids) implements CustomPayload {
	public static final Id<CPRegistryList> ID = new Id<>(VoxEdit.id("registry_list"));
	public static final PacketCodec<ByteBuf, CPRegistryList> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, CPRegistryList::requestID, Identifier.PACKET_CODEC.collect(PacketCodecs.toList()), CPRegistryList::ids, CPRegistryList::new);
	static {
		PayloadTypeRegistry.playS2C().register(ID, CODEC);
	}
	
	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
