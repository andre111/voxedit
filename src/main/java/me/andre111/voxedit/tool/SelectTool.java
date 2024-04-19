package me.andre111.voxedit.tool;

import java.util.List;

import me.andre111.voxedit.state.ServerState;
import me.andre111.voxedit.tool.data.Context;
import me.andre111.voxedit.tool.data.RaycastTargets;
import me.andre111.voxedit.tool.data.Target;
import me.andre111.voxedit.tool.data.ToolConfig;
import me.andre111.voxedit.tool.data.ToolSettings;
import net.minecraft.server.network.ServerPlayerEntity;

public class SelectTool extends Tool {
	public SelectTool() {
		super(Properties.of(ToolSettings.SHAPE, ToolSettings.TARGET_FLUIDS).noPresets());
	}

	@Override
	public RaycastTargets getRaycastTargets(ToolConfig config) {
		if(ToolSettings.TARGET_FLUIDS.get(config)) {
			return RaycastTargets.BLOCKS_AND_FLUIDS;
		} else {
			return RaycastTargets.BLOCKS_ONLY;
		}
	}

	@Override
	public void performAction(ServerPlayerEntity player, Action action, List<Target> targets, Context context, ToolConfig config, ServerState state) {
		// TODO Auto-generated method stub
		
	}

}
