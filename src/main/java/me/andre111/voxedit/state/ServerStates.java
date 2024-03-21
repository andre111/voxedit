package me.andre111.voxedit.state;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.network.ServerPlayerEntity;

public class ServerStates {
	private static final Map<UUID, ServerState> STATES = new HashMap<>();
	
	public static ServerState get(ServerPlayerEntity player) {
		if(!STATES.containsKey(player.getUuid())) STATES.put(player.getUuid(), new ServerState(buf -> {
			//ServerPlayNetworking.send(player, VoxEdit.id("state"), buf);
		}));
		return STATES.get(player.getUuid());
	}
}
