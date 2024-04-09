package me.andre111.voxedit.client;

import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class ClientStates {
	private static ClientState INSTANCE;
	
	public static void recreateInstance(WrapperLookup registryLookup) {
		INSTANCE = new ClientState(registryLookup);
	}
	
	public static ClientState instance() {
		return INSTANCE;
	}
}
