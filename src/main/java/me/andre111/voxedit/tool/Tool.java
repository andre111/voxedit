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

import java.util.List;
import java.util.Set;

import com.mojang.serialization.Codec;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.editor.UndoRecordingStructureWorldAccess;
import me.andre111.voxedit.tool.config.ToolConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public abstract class Tool<TC extends ToolConfig<TC>, T extends Tool<TC, T>> {
	private final Codec<ConfiguredTool<TC, T>> codec;
	private final TC defaultConfig;

    @SuppressWarnings("unchecked")
	public Tool(Codec<TC> configCodec, TC defaultConfig) {
    	this.codec = configCodec.optionalFieldOf("config", defaultConfig).xmap(config -> new ConfiguredTool<TC, T>((T) this, (TC) config), ct -> (TC) ct.config()).codec();
    	this.defaultConfig = defaultConfig;
    }

    public final Codec<ConfiguredTool<TC, T>> getCodec() {
        return this.codec;
    }
    
    public final TC getDefaultConfig() {
    	return defaultConfig;
    }
    
    @SuppressWarnings("unchecked")
	public final ConfiguredTool<TC, T> getDefault() {
    	return new ConfiguredTool<TC, T>((T) this, defaultConfig);
    }

    
    @SuppressWarnings("unchecked")
	public final ConfiguredTool<TC, T> getWith(TC config) {
    	return new ConfiguredTool<TC, T>((T) this, config);
    }
	
	public final Identifier id() {
		return VoxEdit.TOOL_REGISTRY.getId(this);
	}
	
	public final Text asText() {
		Identifier id = id();
		return Text.translatable("voxedit.tool."+id.toTranslationKey());
	}
    
    public List<TC> getAdditionalCreativeMenuConfigs() {
    	return List.of();
    }

	public abstract void rightClick(UndoRecordingStructureWorldAccess world, PlayerEntity player, BlockHitResult target, TC config, Set<BlockPos> positions);
	public abstract void leftClick(UndoRecordingStructureWorldAccess world, PlayerEntity player, BlockHitResult target, TC config, Set<BlockPos> positions);
	public abstract Set<BlockPos> getBlockPositions(BlockView world, BlockHitResult target, TC config);
}
