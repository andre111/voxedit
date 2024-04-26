/*
 * Copyright (c) 2024 André Schweiger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.andre111.voxedit.tool.data;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record ToolConfig(Map<String, String> values) {
	public static final Codec<ToolConfig> CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING).xmap(ToolConfig::new, ToolConfig::values);
	public static final PacketCodec<ByteBuf, ToolConfig> PACKET_CODEC = PacketCodecs.map(s -> (Map<String, String>) new HashMap<String, String>(), PacketCodecs.STRING, PacketCodecs.STRING).xmap(ToolConfig::new, ToolConfig::values);

	public ToolConfig withRaw(String key, String value) {
		Map<String, String> newValues = new HashMap<>(values);
		newValues.put(key, value);
		return new ToolConfig(newValues);
	}
	
	public <V> ToolConfig with(ToolSetting<V> setting, V value) {
		return setting.with(this, value);
	}
	
	public <V> ToolConfig modify(ToolSetting<V> setting, Function<V, V> modifier) {
		return setting.with(this, modifier.apply(setting.get(this)));
	}
}
