package me.andre111.voxedit.tool.data;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record ToolConfig(Map<String, String> values) {
	public static final Codec<ToolConfig> CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING).xmap(ToolConfig::new, ToolConfig::values);
	public static final PacketCodec<ByteBuf, ToolConfig> PACKET_CODEC = PacketCodecs.map(s -> (Map<String, String>) new HashMap<String, String>(), PacketCodecs.STRING, PacketCodecs.STRING).xmap(ToolConfig::new, ToolConfig::values);

	public ToolConfig with(String key, String value) {
		Map<String, String> newValues = new HashMap<>(values);
		newValues.put(key, value);
		return new ToolConfig(newValues);
	}
}
