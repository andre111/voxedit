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
