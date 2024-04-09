package me.andre111.voxedit.network;

import io.netty.buffer.ByteBuf;
import me.andre111.voxedit.VoxEdit;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CPSchematic(Identifier id, NbtCompound nbt) implements CustomPayload {
	public static final Id<CPSchematic> ID = new Id<>(VoxEdit.id("schematic"));
	public static final PacketCodec<ByteBuf, CPSchematic> CODEC = PacketCodec.tuple(
			Identifier.PACKET_CODEC, CPSchematic::id, 
			PacketCodecs.NBT_COMPOUND, CPSchematic::nbt, 
			CPSchematic::new);
	static {
		PayloadTypeRegistry.playS2C().register(ID, CODEC);
	}
	
	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
