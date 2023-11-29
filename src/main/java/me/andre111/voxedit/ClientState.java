package me.andre111.voxedit;

import java.util.Set;

import me.andre111.voxedit.tool.ConfiguredTool;
import me.andre111.voxedit.tool.Tool;
import me.andre111.voxedit.tool.config.ToolConfig;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class ClientState {
	public static ClientPlayerEntity player;
	public static ToolItem.Data active;
	public static BlockHitResult target;
	
	public static Set<BlockPos> positions;
	public static int ticks;
	
	public static int undoSize;
	public static int undoIndex;
	
	public static float cameraSpeed = 2f;
	
	@SuppressWarnings("unchecked")
	public static <TC extends ToolConfig, T extends Tool<TC, T>> void sendConfigChange(TC newConfig) {
		if(active == null) return;
		if(!active.selected().config().getClass().isAssignableFrom(newConfig.getClass())) return;
		Networking.clientSendToolChange(new ConfiguredTool<TC, T>((T) active.selected().tool(), newConfig));
	}
}
