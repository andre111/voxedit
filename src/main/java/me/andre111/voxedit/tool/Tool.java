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
package me.andre111.voxedit.tool;

import java.util.List;
import java.util.Map;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.data.Context;
import me.andre111.voxedit.data.RaycastTargets;
import me.andre111.voxedit.data.Setting;
import me.andre111.voxedit.data.Target;
import me.andre111.voxedit.data.Config;
import me.andre111.voxedit.data.Configurable;
import me.andre111.voxedit.state.ServerState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public abstract class Tool implements Configurable<Tool> {
	private final Properties properties;

	public Tool(Properties.Builder builder) {
		this(builder.create());
    }
	
	public Tool(Properties properties) {
		this.properties = properties;
    }
	
	public final Properties properties() {
		return properties;
	}

	@Override
    public final List<Setting<?>> getSettings() {
    	return properties.settings();
    }

	@Override
	public Configurable.Type<Tool> getType() {
		return VoxEdit.TYPE_TOOL;
	}
    
	public final Identifier id() {
		return VoxEdit.TOOL_REGISTRY.getId(this);
	}
	
	public final Text asText() {
		Identifier id = id();
		return Text.translatable("voxedit.tool."+id.toTranslationKey());
	}
    
	public Map<String, Config> getPresets() {
		return Map.of();
	}
	
	public abstract RaycastTargets getRaycastTargets(Config config);
	public abstract void performAction(ServerPlayerEntity player, Action action, List<Target> targets, Context context, Config config, ServerState state);
	public void changedSetting(Setting<?> setting, Config config) {}
	
	public static enum Action {
		ADD_OR_MODIFY,
		REMOVE,
		PREVIEW,
		APPLY_PREVIEW;
	}
}
