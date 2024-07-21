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

import java.util.List;

import io.netty.buffer.ByteBuf;
import me.andre111.voxedit.VECodecs;
import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.data.Configured;
import me.andre111.voxedit.data.Context;
import me.andre111.voxedit.data.Target;
import me.andre111.voxedit.tool.Tool;
import me.andre111.voxedit.tool.Tool.Action;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record CPAction(Configured<Tool> tool, List<Target> targets, Context context, Action action) implements CustomPayload {
	public static final Id<CPAction> ID = new Id<>(VoxEdit.id("action"));
	public static final PacketCodec<ByteBuf, CPAction> CODEC = PacketCodec.tuple(
			VECodecs.CONFIGURED_TOOL_PACKET, CPAction::tool, 
			Target.PACKET_CODEC.collect(PacketCodecs.toList(VoxEdit.MAX_TARGETS)), CPAction::targets,
			Context.PACKET_CODEC, CPAction::context,
			PacketCodecs.indexed(i -> Action.values()[i], Action::ordinal), CPAction::action,
			CPAction::new);
	static {
		PayloadTypeRegistry.playC2S().register(ID, CODEC);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
