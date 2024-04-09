/*
 * Copyright (c) 2023 André Schweiger
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
package me.andre111.voxedit.tool;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.tool.data.ToolConfig;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record ConfiguredTool(Tool tool, ToolConfig config) {
	public static final Codec<ConfiguredTool> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
					VoxEdit.TOOL_REGISTRY.getCodec().fieldOf("tool").forGetter(ConfiguredTool::tool),
					ToolConfig.CODEC.fieldOf("config").forGetter(ConfiguredTool::config)
			)
			.apply(instance, ConfiguredTool::new));
	public static final PacketCodec<ByteBuf, ConfiguredTool> PACKET_CODEC = PacketCodec.tuple(
			PacketCodecs.codec(VoxEdit.TOOL_REGISTRY.getCodec()), ConfiguredTool::tool,
			ToolConfig.PACKET_CODEC, ConfiguredTool::config,
			ConfiguredTool::new);
}
