package me.andre111.voxedit;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoxEdit implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("voxedit");

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");
	}
}
