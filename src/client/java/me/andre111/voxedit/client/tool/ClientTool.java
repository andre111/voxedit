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

	public void mouseMoved(int button, Context context, ToolConfig config) {}
	public void mousePressed(int button, Context context, ToolConfig config) {}
	public void mouseReleased(int button, Context context, ToolConfig config) {}
	
	public abstract void mouseTargetMoved(Target target, Context context, ToolConfig config);
	public abstract void mouseTargetClicked(int button, Target target, Context context, ToolConfig config);
	public abstract boolean cancel();
}
