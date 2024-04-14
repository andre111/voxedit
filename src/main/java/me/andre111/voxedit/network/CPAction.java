package me.andre111.voxedit.network;

import java.util.List;

import io.netty.buffer.ByteBuf;
import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.tool.ConfiguredTool;
import me.andre111.voxedit.tool.Tool.Action;
import me.andre111.voxedit.tool.data.Context;
import me.andre111.voxedit.tool.data.Target;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record CPAction(ConfiguredTool tool, List<Target> targets, Context context, Action action) implements CustomPayload {
	public static final Id<CPAction> ID = new Id<>(VoxEdit.id("action"));
	public static final PacketCodec<ByteBuf, CPAction> CODEC = PacketCodec.tuple(
			PacketCodecs.codec(ConfiguredTool.CODEC), CPAction::tool, 
			Target.PACKET_CODEC.collect(PacketCodecs.toList(VoxEdit.MAX_TARGETS)), CPAction::targets,
			Context.PACKET_CODEC, CPAction::context,
			PacketCodecs.indexed(i -> Action.values()[i], Action::ordinal), CPAction::action,
			CPAction::new);
	static {
		PayloadTypeRegistry.playC2S().register(ID, CODEC);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
