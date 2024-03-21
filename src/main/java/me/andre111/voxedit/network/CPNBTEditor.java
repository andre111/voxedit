package me.andre111.voxedit.network;

import io.netty.buffer.ByteBuf;
import me.andre111.voxedit.VoxEdit;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record CPNBTEditor(boolean apply, NbtCompound nbt) implements CustomPayload {
	public static final Id<CPNBTEditor> ID = new Id<>(VoxEdit.id("nbteditor"));
	public static final PacketCodec<ByteBuf, CPNBTEditor> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, CPNBTEditor::apply, PacketCodecs.NBT_COMPOUND, CPNBTEditor::nbt, CPNBTEditor::new);
	static {
		PayloadTypeRegistry.playC2S().register(ID, CODEC);
		PayloadTypeRegistry.playS2C().register(ID, CODEC);
	}
	
	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
