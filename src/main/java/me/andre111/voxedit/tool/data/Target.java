package me.andre111.voxedit.tool.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public record Target(BlockPos pos, Direction side) {
	public static final Codec<Target> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
					BlockPos.CODEC.fieldOf("pos").forGetter(Target::pos),
					Direction.CODEC.fieldOf("side").forGetter(Target::side)
			)
			.apply(instance, Target::new));
	public static final PacketCodec<ByteBuf, Target> PACKET_CODEC = PacketCodec.tuple(
			BlockPos.PACKET_CODEC, Target::pos, 
			Direction.PACKET_CODEC, Target::side, 
			Target::new);
}
