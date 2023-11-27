package me.andre111.voxedit;

import java.util.Set;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class ClientState {
	public static ClientPlayerEntity player;
	public static ToolState active;
	public static BlockHitResult target;
	
	public static Set<BlockPos> positions;
	public static int ticks;
	
	public static int undoSize;
	public static int undoIndex;
}
