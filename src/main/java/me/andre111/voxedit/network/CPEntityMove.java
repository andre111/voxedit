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

import java.util.UUID;

import org.joml.Vector3f;

import io.netty.buffer.ByteBuf;
import me.andre111.voxedit.VoxEdit;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

public record CPEntityMove(UUID uuid, Vector3f pos, float yaw) implements CustomPayload {
	public static final Id<CPEntityMove> ID = new Id<>(VoxEdit.id("entity_move"));
	public static final PacketCodec<ByteBuf, CPEntityMove> CODEC = PacketCodec.tuple(
		Uuids.PACKET_CODEC, CPEntityMove::uuid,
		PacketCodecs.VECTOR3F, CPEntityMove::pos,
		PacketCodecs.FLOAT, CPEntityMove::yaw,
		CPEntityMove::new);
	
	static {
		PayloadTypeRegistry.playC2S().register(ID, CODEC);
	}
	
	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
