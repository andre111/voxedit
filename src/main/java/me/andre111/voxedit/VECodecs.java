package me.andre111.voxedit;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import me.andre111.voxedit.data.Configured;
import me.andre111.voxedit.filter.Filter;
import me.andre111.voxedit.tool.Tool;
import net.minecraft.network.codec.PacketCodec;

public class VECodecs {
	public static final Codec<Configured<Tool>> CONFIGURED_TOOL = Configured.createCodec(VoxEdit.TOOL_REGISTRY.getCodec(), "tool");
	public static final PacketCodec<ByteBuf, Configured<Tool>> CONFIGURED_TOOL_PACKET = Configured.createPacketCodec(VoxEdit.TOOL_REGISTRY.getCodec());
	
	public static final Codec<Configured<Filter>> CONFIGURED_FILTER = Configured.createCodec(VoxEdit.FILTER_REGISTRY.getCodec(), "filter");
	public static final PacketCodec<ByteBuf, Configured<Filter>> CONFIGURED_FILTER_PACKET = Configured.createPacketCodec(VoxEdit.FILTER_REGISTRY.getCodec());
}
