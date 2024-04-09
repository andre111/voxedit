/*
 * Copyright (c) 2023 André Schweiger
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

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.editor.EditorWorld;
import me.andre111.voxedit.tool.data.Context;
import me.andre111.voxedit.tool.data.Target;
import me.andre111.voxedit.tool.data.ToolConfig;
import me.andre111.voxedit.tool.data.ToolSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public abstract class Tool {
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

    public final List<ToolSetting<?>> getSettings() {
    	return properties.settings();
    }
    
    public final boolean has(ToolSetting<?> setting) {
    	for(var s : properties.settings()) if(s == setting) return true;
    	return false;
    }
    
    public final ToolConfig getDefaultConfig() {
    	ToolConfig config = new ToolConfig(new HashMap<>());
    	for(var setting : properties.settings()) {
    		config = setting.withDefaultValue(config);
    	}
    	return config;
    }
    
    public final boolean isValid(ToolConfig config) {
    	for(var setting : properties.settings()) {
    		if(!setting.isValidOrMissing(config)) return false;
    	}
    	return true;
    }
    
	public final Identifier id() {
		return VoxEdit.TOOL_REGISTRY.getId(this);
	}
	
	public final Text asText() {
		Identifier id = id();
		return Text.translatable("voxedit.tool."+id.toTranslationKey());
	}
    
    /*public List<TC> getAdditionalPremadeConfigs() {
    	return List.of();
    }*/

	public abstract void place(EditorWorld world, PlayerEntity player, Target target, Context context, ToolConfig config, Set<BlockPos> positions);
	public abstract void remove(EditorWorld world, PlayerEntity player, Target target, Context context, ToolConfig config, Set<BlockPos> positions);
	public abstract Set<BlockPos> getBlockPositions(BlockView world, Target target, Context context, ToolConfig config);
}
