package me.andre111.voxedit.client.tool;

import java.util.List;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.state.ServerState;
import me.andre111.voxedit.tool.Properties;
import me.andre111.voxedit.tool.Properties.Builder;
import me.andre111.voxedit.tool.Tool;
import me.andre111.voxedit.tool.data.Context;
import me.andre111.voxedit.tool.data.Target;
import me.andre111.voxedit.tool.data.ToolConfig;
import net.minecraft.server.network.ServerPlayerEntity;

public abstract class ClientTool extends Tool {
	public ClientTool(Properties properties) {
		super(properties);
	}
	public ClientTool(Builder builder) {
		super(builder);
	}
	
	@Override
	public final void performAction(ServerPlayerEntity player, Action action, List<Target> targets, Context context, ToolConfig config, ServerState state) {
		VoxEdit.LOGGER.error("Trying to perform server action on ClientTool.");
	}

	public abstract void mouseMoved(Target target, Context context, ToolConfig config);
	public abstract void mouseClicked(int button, Target target, Context context, ToolConfig config);
	public abstract boolean cancel();
}
