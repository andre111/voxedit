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
package me.andre111.voxedit.state;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.andre111.voxedit.VoxEdit;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerStates {
	private static final Map<UUID, ServerState> STATES = new HashMap<>();
	private static boolean FROZEN_STATE = false;
	private static boolean BACKUP_WAS_FROZEN = false;
	
	public static ServerState get(ServerPlayerEntity player) {
		if(!STATES.containsKey(player.getUuid())) STATES.put(player.getUuid(), new ServerState(player.getRegistryManager(), cp -> {
			ServerPlayNetworking.send(player, cp);
		}, VoxEdit.dataPath(player.getServer()).resolve(player.getUuidAsString()+"/")));
		return STATES.get(player.getUuid());
	}
	
	public static void updateTickFreeze(MinecraftServer server) {
		// determine if ticks should be frozen (any player has the editor open)
		boolean shouldFreeze = false;
		for(var e : STATES.entrySet()) {
			var player = server.getPlayerManager().getPlayer(e.getKey());
			if(player != null && !player.isDisconnected() && e.getValue().editorActive()) shouldFreeze = true;
		}
		
		// toggle state, while keeping the game frozen if it already was before an editor was opened
		if(shouldFreeze != FROZEN_STATE) {
			if(FROZEN_STATE == false) {
				BACKUP_WAS_FROZEN = server.getTickManager().isFrozen();
			}
			
			FROZEN_STATE = shouldFreeze;
			
			if(shouldFreeze) server.getTickManager().setFrozen(true);
			else server.getTickManager().setFrozen(BACKUP_WAS_FROZEN);
		}
	}
}
