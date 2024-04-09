package me.andre111.voxedit.network;

import io.netty.buffer.ByteBuf;
import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.state.Selection;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record CPSetSelection(Selection selection) implements CustomPayload {
	public static final Id<CPSetSelection> ID = new Id<>(VoxEdit.id("set_selection"));
	public static final PacketCodec<ByteBuf, CPSetSelection> CODEC = PacketCodecs.codec(Selection.CODEC.xmap(CPSetSelection::new, CPSetSelection::selection));
	static {
		PayloadTypeRegistry.playC2S().register(ID, CODEC);
		PayloadTypeRegistry.playS2C().register(ID, CODEC);
	}
	
	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
