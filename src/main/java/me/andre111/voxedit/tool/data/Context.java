package me.andre111.voxedit.tool.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;

public record Context(BlockPalette palette, BlockPalette filter) {
	public static final Codec<Context> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
					BlockPalette.CODEC.fieldOf("palette").forGetter(Context::palette),
					BlockPalette.CODEC.fieldOf("filter").forGetter(Context::filter)
			)
			.apply(instance, Context::new));
	public static final PacketCodec<ByteBuf, Context> PACKET_CODEC = PacketCodec.tuple(
			BlockPalette.PACKET_CODEC, Context::palette, 
			BlockPalette.PACKET_CODEC, Context::filter, 
			Context::new);
}
