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
package me.andre111.voxedit.network;

import io.netty.buffer.ByteBuf;
import me.andre111.voxedit.VoxEdit;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record CPStatusMessage(Text status) implements CustomPayload {
	public static final Id<CPStatusMessage> ID = new Id<>(VoxEdit.id("status_message"));
	public static final PacketCodec<ByteBuf, CPStatusMessage> CODEC = TextCodecs.PACKET_CODEC.xmap(CPStatusMessage::new, CPStatusMessage::status);
	
	static {
		PayloadTypeRegistry.playS2C().register(ID, CODEC);
	}
	
	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
