package me.andre111.voxedit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import net.minecraft.nbt.NbtCompound;

public class ServerState {
	public static Map<UUID, Consumer<NbtCompound>> NBTEDITOR_TARGETS = new HashMap<>();
}
