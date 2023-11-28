package me.andre111.voxedit.tool;

import com.mojang.serialization.Codec;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.tool.config.ToolConfig;

public record ConfiguredTool<TC extends ToolConfig, T extends Tool<TC, T>>(T tool, TC config) {
	public static final Codec<ConfiguredTool<?, ?>> CODEC = VoxEdit.TOOL_REGISTRY.getCodec().dispatch(configuredTool -> configuredTool.tool, Tool::getCodec);
}
