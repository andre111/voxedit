package me.andre111.voxedit.data;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import me.andre111.voxedit.VoxEdit;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record Configured<T extends Configurable<T>>(T value, Config config) {
	public static final Codec<Configured<?>> GENERIC_CODEC = VoxEdit.CONFIGURABLE_TYPE_REGISTRY.getCodec().dispatch(
			configured -> configured.value().getType(), 
			type -> getInstanceCodec(type));
	
	private static final Map<Configurable.Type<?>, MapCodec<?>> INSTANCE_CODEC_CACHE = new HashMap<>();
	@SuppressWarnings("unchecked")
	private static MapCodec<Configured<?>> getInstanceCodec(Configurable.Type<?> type) {
		return (MapCodec<Configured<?>>) INSTANCE_CODEC_CACHE.computeIfAbsent(type, t -> createCodec(t.baseCodec(), "type").fieldOf("instance"));
	}
	
	public static <T extends Configurable<T>> Codec<Configured<T>> createCodec(Codec<T> baseCodec, String baseName) {
		return RecordCodecBuilder.create(instance -> instance
		.group(
			baseCodec.fieldOf(baseName).forGetter(Configured<T>::value),
			Config.CODEC.fieldOf("config").forGetter(Configured<T>::config)
		)
		.apply(instance, Configured<T>::new));
	}
	
	public static <T extends Configurable<T>> PacketCodec<ByteBuf, Configured<T>> createPacketCodec(Codec<T> baseCodec) {
		return PacketCodec.tuple(
				PacketCodecs.codec(baseCodec), Configured<T>::value,
				Config.PACKET_CODEC, Configured<T>::config,
				Configured<T>::new);
	}
	
	public boolean isValid() {
		return value.isValid(config);
	}
	
	public Configured<T> with(Config newConfig) {
		return value.isValid(newConfig) ? new Configured<>(value, newConfig) : this;
	}
}
