package me.andre111.voxedit.network;

import java.util.List;

import io.netty.buffer.ByteBuf;
import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.editor.EditStats;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record CPHistoryInfo(List<EditStats> history, int index, boolean append) implements CustomPayload {
	public static final Id<CPHistoryInfo> ID = new Id<>(VoxEdit.id("history_info"));
	public static final PacketCodec<ByteBuf, CPHistoryInfo> CODEC = PacketCodec.tuple(
			EditStats.WITHOUT_SCHEMATIC_PACKET_CODEC.collect(PacketCodecs.toList()), CPHistoryInfo::history,
			PacketCodecs.INTEGER, CPHistoryInfo::index,
			PacketCodecs.BOOL, CPHistoryInfo::append,
			CPHistoryInfo::new);
	static {
		PayloadTypeRegistry.playS2C().register(ID, CODEC);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
