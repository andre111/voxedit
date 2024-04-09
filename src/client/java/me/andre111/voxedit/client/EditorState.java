package me.andre111.voxedit.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.state.Schematic;
import me.andre111.voxedit.tool.ConfiguredTool;
import me.andre111.voxedit.tool.Tool;
import me.andre111.voxedit.tool.data.BlockPalette;
import me.andre111.voxedit.tool.data.Context;
import me.andre111.voxedit.tool.data.Target;
import me.andre111.voxedit.tool.data.ToolConfig;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Identifier;

public class EditorState {
	public static final Event<Consumer<Tool>> CHANGE_TOOL = EventFactory.createArrayBacked(Consumer.class, callbacks -> tool -> {
		for (Consumer<Tool> callback : callbacks) {
			callback.accept(tool);
		}
	});
	public static final Event<Consumer<ToolConfig>> CHANGE_TOOL_CONFIG = EventFactory.createArrayBacked(Consumer.class, callbacks -> toolConfig -> {
		for (Consumer<ToolConfig> callback : callbacks) {
			callback.accept(toolConfig);
		}
	});
	public static final Event<Consumer<List<Target>>> CHANGE_TARGETS = EventFactory.createArrayBacked(Consumer.class, callbacks -> targets -> {
		for (Consumer<List<Target>> callback : callbacks) {
			callback.accept(targets);
		}
	});
	public static final Event<Consumer<BlockPalette>> CHANGE_BLOCK_PALETTE = EventFactory.createArrayBacked(Consumer.class, callbacks -> blockPalette -> {
		for (Consumer<BlockPalette> callback : callbacks) {
			callback.accept(blockPalette);
		}
	});
	public static final Event<Consumer<BlockPalette>> CHANGE_FILTER = EventFactory.createArrayBacked(Consumer.class, callbacks -> blockPalette -> {
		for (Consumer<BlockPalette> callback : callbacks) {
			callback.accept(blockPalette);
		}
	});
	public static final Event<BiConsumer<Identifier, Schematic>> CHANGE_SCHEMATIC = EventFactory.createArrayBacked(BiConsumer.class, callbacks -> (id, schematic) -> {
		for (BiConsumer<Identifier, Schematic> callback : callbacks) {
			callback.accept(id, schematic);
		}
	});
	
	private static Tool tool;
	private static ToolConfig toolConfig;
	private static List<Target> targets = new ArrayList<>();
	private static BlockPalette blockPalette = new BlockPalette(BlockPalette.DEFAULT.getEntries());
	private static BlockPalette filter = new BlockPalette();
	private static Map<Identifier, Schematic> schematics = new HashMap<>();
	
	public static Tool tool() {
		return tool;
	}
	
	public static void tool(Tool tool) {
		EditorState.tool = tool;
		EditorState.toolConfig = tool.getDefaultConfig();
		EditorState.targets.clear();
		
		CHANGE_TOOL.invoker().accept(tool);
		CHANGE_TOOL_CONFIG.invoker().accept(toolConfig);
		CHANGE_TARGETS.invoker().accept(null);
	}
	
	public static ToolConfig toolConfig() {
		return toolConfig;
	}
	
	public static void toolConfig(ToolConfig toolConfig) {
		if(tool == null) return;
		if(!tool.isValid(toolConfig)) return;
		
		EditorState.toolConfig = toolConfig;
		CHANGE_TOOL_CONFIG.invoker().accept(toolConfig);
	}
	
	public static ConfiguredTool configuredTool() {
		if(tool == null) return null;
		if(toolConfig == null) return null;
		if(!tool.isValid(toolConfig)) return null;
		
		return new ConfiguredTool(tool, toolConfig);
	}
	
	public static List<Target> targets() {
		return targets;
	}
	
	public static void target(Target target, boolean add) {
		if(!add) EditorState.targets.clear();
		if(EditorState.targets.size() == VoxEdit.MAX_TARGETS) return;
		EditorState.targets.add(target);
		CHANGE_TARGETS.invoker().accept(targets);
	}
	
	public static void clearTargets() {
		EditorState.targets.clear();
		CHANGE_TARGETS.invoker().accept(targets);
	}

	public static BlockPalette blockPalette() {
		return blockPalette;
	}
	
	public static void blockPalette(BlockPalette blockPalette) {
		EditorState.blockPalette = blockPalette;
		CHANGE_BLOCK_PALETTE.invoker().accept(blockPalette);
	}

	public static BlockPalette filter() {
		return filter;
	}
	
	public static void filter(BlockPalette filter) {
		EditorState.blockPalette = filter;
		CHANGE_FILTER.invoker().accept(filter);
	}
	
	public static Schematic schematic(Identifier id) {
		return schematics.get(id);
	}
	
	public static void schematic(Identifier id, Schematic schematic) {
		if(schematic == null) EditorState.schematics.remove(id);
		else EditorState.schematics.put(id, schematic);
		CHANGE_SCHEMATIC.invoker().accept(id, schematic);
	}
	
	public static Context context() {
		return new Context(blockPalette, filter);
	}
}
