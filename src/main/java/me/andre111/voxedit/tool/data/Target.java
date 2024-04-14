package me.andre111.voxedit.tool.data;

import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public record Target(Optional<BlockPos> pos, Optional<Direction> side, Optional<UUID> entity) {
	public static final Codec<Target> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
					BlockPos.CODEC.optionalFieldOf("pos").forGetter(Target::pos),
					Direction.CODEC.optionalFieldOf("side").forGetter(Target::side),
					Uuids.CODEC.optionalFieldOf("entity").forGetter(Target::entity)
			)
			.apply(instance, Target::new));
	public static final PacketCodec<ByteBuf, Target> PACKET_CODEC = PacketCodec.tuple(
			PacketCodecs.optional(BlockPos.PACKET_CODEC), Target::pos, 
			PacketCodecs.optional(Direction.PACKET_CODEC), Target::side, 
			PacketCodecs.optional(Uuids.PACKET_CODEC), Target::entity,
			Target::new);
	
	public BlockPos getBlockPos() {
		return pos.get();
	}
	
	public Direction getSide() {
		return side.get();
	}
	
	public UUID getEntity() {
		return entity.get();
	}
}
