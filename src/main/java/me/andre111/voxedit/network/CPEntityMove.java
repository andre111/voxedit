package me.andre111.voxedit.network;

import java.util.UUID;

import org.joml.Vector3f;

import io.netty.buffer.ByteBuf;
import me.andre111.voxedit.VoxEdit;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

public record CPEntityMove(UUID uuid, Vector3f pos, float yaw) implements CustomPayload {
	public static final Id<CPEntityMove> ID = new Id<>(VoxEdit.id("entity_move"));
	public static final PacketCodec<ByteBuf, CPEntityMove> CODEC = PacketCodec.tuple(
		Uuids.PACKET_CODEC, CPEntityMove::uuid,
		PacketCodecs.VECTOR3F, CPEntityMove::pos,
		PacketCodecs.FLOAT, CPEntityMove::yaw,
		CPEntityMove::new);
	
	static {
		PayloadTypeRegistry.playC2S().register(ID, CODEC);
	}
	
	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
