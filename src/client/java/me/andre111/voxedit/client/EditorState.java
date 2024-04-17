package me.andre111.voxedit.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import me.andre111.voxedit.Presets;
import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.VoxEditUtil;
import me.andre111.voxedit.editor.EditStats;
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
import net.minecraft.util.math.BlockPos;

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
	public static final Event<Runnable> UPDATE_HISTORY = EventFactory.createArrayBacked(Runnable.class, callbacks -> () -> {
		for (Runnable callback : callbacks) {
			callback.run();
		}
	});

	private static Tool tool;
	private static List<Target> targets = new ArrayList<>();
	private static Set<BlockPos> positions = new HashSet<>();
	private static BlockPalette filter = new BlockPalette();
	private static Map<Identifier, Schematic> schematics = new HashMap<>();
	private static List<EditStats> history = new ArrayList<>();
	private static int historyIndex = -1;
	private static float cameraSpeed = 2f;

	private static Persistant persistant;
	private static int ticks;

	public static Tool tool() {
		return tool;
	}

	public static void tool(Tool tool) {
		EditorState.tool = tool;
		ToolConfig toolConfig = persistant().active(tool);
		EditorState.targets.clear();

		CHANGE_TOOL.invoker().accept(tool);
		CHANGE_TOOL_CONFIG.invoker().accept(toolConfig);
		CHANGE_TARGETS.invoker().accept(null);
	}

	public static ToolConfig toolConfig() {
		if(tool == null) return null;
		return persistant().active(tool);
	}

	public static void toolConfig(ToolConfig toolConfig) {
		if(tool == null) return;
		if(!tool.isValid(toolConfig)) return;

		toolConfig = new ToolConfig(new HashMap<>(toolConfig.values()));
		persistant().active(tool, toolConfig);
		CHANGE_TOOL_CONFIG.invoker().accept(toolConfig);
	}

	public static ConfiguredTool configuredTool() {
		if(tool == null) return null;
		ToolConfig toolConfig = persistant().active(tool);
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

	public static Set<BlockPos> positions() {
		return positions;
	}

	public static void positions(Set<BlockPos> positions) {
		EditorState.positions = positions;
	}

	public static BlockPalette blockPalette() {
		return persistant().palette();
	}

	public static void blockPalette(BlockPalette blockPalette) {
		persistant().palette(blockPalette);
		CHANGE_BLOCK_PALETTE.invoker().accept(blockPalette);
	}

	public static BlockPalette filter() {
		return filter;
	}

	public static void filter(BlockPalette filter) {
		EditorState.filter = filter;
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
		return new Context(persistant().palette(), filter);
	}

	public static List<EditStats> history() {
		return Collections.unmodifiableList(history);
	}

	public static int historyIndex() {
		return historyIndex;
	}

	public static void history(List<EditStats> history, int historyIndex, boolean append) {
		if(!append) EditorState.history.clear();
		EditorState.history.addAll(history);
		EditorState.historyIndex = historyIndex;
		UPDATE_HISTORY.invoker().run();
	}

	public static float cameraSpeed() {
		return cameraSpeed;
	}

	public static void cameraSpeed(float cameraSpeed) {
		EditorState.cameraSpeed = cameraSpeed;
	}

	public static Persistant persistant() {
		if(persistant == null) persistant = new Persistant();
		return persistant;
	}

	public static void tick() {
		if(ticks++ == 20 * 3) {
			persistant().persistIfModified();
			ticks = 0;
		}
	}

	public static final class Persistant {
		private Map<Identifier, ToolConfig> TOOL_ACTIVE = new HashMap<>();
		private Map<Identifier, Map<String, ToolConfig>> TOOL_PRESETS = new HashMap<>();

		private BlockPalette PALETTE_ACTIVE = new BlockPalette(BlockPalette.DEFAULT.getEntries());
		private Map<String, BlockPalette> PALETTE_PRESETS = new HashMap<>();

		private boolean modified = false;

		private Persistant() {
			// load tools
			VoxEdit.TOOL_REGISTRY.forEach(tool -> {
				Identifier id = tool.id();

				Path dir = VoxEditClient.dataPath().resolve("tools/"+tool.id().getNamespace()+"/"+tool.id().getPath()+"/");
				Path current = dir.resolve("current.json");
				TOOL_ACTIVE.put(tool.id(), VoxEditUtil.readJson(current, ToolConfig.CODEC, tool.getDefaultConfig()));

				// load saved presets
				Map<String, ToolConfig> presets = new HashMap<>();
				try {
					if(Files.exists(dir.resolve("presets/"))) {
						Files.list(dir.resolve("presets/")).forEach(path -> {
							String fileName = path.getFileName().toString();
							if(!fileName.endsWith(".json")) return;
							String name = fileName.substring(0, fileName.length()-5);

							ToolConfig config = VoxEditUtil.readJson(path, ToolConfig.CODEC, null);
							if(config == null) {
								System.err.println("Invalid preset: "+name+" for "+id);
								return;
							}

							presets.put(name, config);
						});
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// add included presets (if not already present)
				if(!presets.containsKey("Default")) {
					presets.put("Default", tool.getDefaultConfig());
				}
				for(var e : tool.getPresets().entrySet()) {
					if(!presets.containsKey(e.getKey())) {
						presets.put(e.getKey(), e.getValue());
					}
				}

				TOOL_PRESETS.put(id, presets);
			});

			// load palettes
			PALETTE_ACTIVE = VoxEditUtil.readJson(VoxEditClient.dataPath().resolve("palette.json"), BlockPalette.CODEC, new BlockPalette(BlockPalette.DEFAULT.getEntries()));
			try {
				if(Files.exists(VoxEditClient.dataPath().resolve("palettes/"))) {
					Files.list(VoxEditClient.dataPath().resolve("palettes/")).forEach(path -> {
						String fileName = path.getFileName().toString();
						if(!fileName.endsWith(".json")) return;
						String name = fileName.substring(0, fileName.length()-5);

						BlockPalette palette = VoxEditUtil.readJson(path, BlockPalette.CODEC, null);
						if(palette == null) {
							System.err.println("Invalid palette: "+name);
							return;
						}

						PALETTE_PRESETS.put(name, palette);
					});
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Presets.addPalettesIfAbsent(PALETTE_PRESETS);
		}

		private void persistIfModified() {
			if(!modified) return;
			modified = false;

			// save tool state
			VoxEdit.TOOL_REGISTRY.forEach(tool -> {
				Path dir = VoxEditClient.dataPath().resolve("tools/"+tool.id().getNamespace()+"/"+tool.id().getPath()+"/");

				Path current = dir.resolve("current.json");
				VoxEditUtil.writeJson(current, ToolConfig.CODEC, active(tool));

				for(var e : presets(tool).entrySet()) {
					VoxEditUtil.writeJson(getPresetPath(tool, e.getKey()), ToolConfig.CODEC, e.getValue());
				}
			});

			// save palette state
			VoxEditUtil.writeJson(VoxEditClient.dataPath().resolve("palette.json"), BlockPalette.CODEC, PALETTE_ACTIVE);
			for(var e : PALETTE_PRESETS.entrySet()) {
				VoxEditUtil.writeJson(getPalettePath(e.getKey()), BlockPalette.CODEC, e.getValue());
			}
		}
		
		private Path getPresetPath(Tool tool, String name) {
			Path dir = VoxEditClient.dataPath().resolve("tools/"+tool.id().getNamespace()+"/"+tool.id().getPath()+"/");
			return dir.resolve("presets/"+name+".json");
		}
		
		private Path getPalettePath(String name) {
			return VoxEditClient.dataPath().resolve("palettes/"+name+".json");
		}

		public Map<String, ToolConfig> presets(Tool tool) {
			return Collections.unmodifiableMap(TOOL_PRESETS.get(tool.id()));
		}
		
		public void preset(Tool tool, String name, ToolConfig preset) {
			TOOL_PRESETS.get(tool.id()).put(name, preset);
			modified = true;
		}
		
		public void deletePreset(Tool tool, String name) {
			TOOL_PRESETS.get(tool.id()).remove(name);
			//TODO: can I handle this better?
			try {
				Path path = getPresetPath(tool, name);
				Files.deleteIfExists(path);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public ToolConfig active(Tool tool) {
			return TOOL_ACTIVE.computeIfAbsent(tool.id(), (id) -> tool.getDefaultConfig());
		}

		public void active(Tool tool, ToolConfig config) {
			TOOL_ACTIVE.put(tool.id(), config);
			modified = true;
		}

		public BlockPalette palette() {
			return PALETTE_ACTIVE;
		}

		public void palette(BlockPalette palette) {
			PALETTE_ACTIVE = palette;
			modified = true;
		}

		public Map<String, BlockPalette> palettePresets() {
			return Collections.unmodifiableMap(PALETTE_PRESETS);
		}
		
		public void palettePreset(String name, BlockPalette palette) {
			PALETTE_PRESETS.put(name, palette);
			modified = true;
		}
		
		public void deletePalettePreset(String name) {
			PALETTE_PRESETS.remove(name);
			//TODO: can I handle this better?
			try {
				Path path = getPalettePath(name);
				Files.deleteIfExists(path);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static record ToolPreset(String name, ToolConfig config) {
	}
}
