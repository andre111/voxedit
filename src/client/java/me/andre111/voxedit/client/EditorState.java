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
import me.andre111.voxedit.VECodecs;
import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.VoxEditUtil;
import me.andre111.voxedit.client.gizmo.Gizmo;
import me.andre111.voxedit.client.gizmo.GizmoHandle;
import me.andre111.voxedit.data.BlockPalette;
import me.andre111.voxedit.data.Context;
import me.andre111.voxedit.data.Target;
import me.andre111.voxedit.data.Config;
import me.andre111.voxedit.data.Configured;
import me.andre111.voxedit.editor.EditStats;
import me.andre111.voxedit.filter.Filter;
import me.andre111.voxedit.schematic.Schematic;
import me.andre111.voxedit.selection.Selection;
import me.andre111.voxedit.tool.Tool;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class EditorState {
	public static final Event<Consumer<Tool>> CHANGE_TOOL = EventFactory.createArrayBacked(Consumer.class, callbacks -> tool -> {
		for (Consumer<Tool> callback : callbacks) {
			callback.accept(tool);
		}
	});
	public static final Event<Consumer<Config>> CHANGE_TOOL_CONFIG = EventFactory.createArrayBacked(Consumer.class, callbacks -> toolConfig -> {
		for (Consumer<Config> callback : callbacks) {
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
	public static final Event<Consumer<Configured<Filter>>> CHANGE_FILTER = EventFactory.createArrayBacked(Consumer.class, callbacks -> blockPalette -> {
		for (Consumer<Configured<Filter>> callback : callbacks) {
			callback.accept(blockPalette);
		}
	});
	public static final Event<Consumer<Gizmo>> CHANGE_SELECTED = EventFactory.createArrayBacked(Consumer.class, callbacks -> selected -> {
		for (Consumer<Gizmo> callback : callbacks) {
			callback.accept(selected);
		}
	});
	public static final Event<Consumer<Gizmo>> MODIFY_GIZMO = EventFactory.createArrayBacked(Consumer.class, callbacks -> gizmo -> {
		for (Consumer<Gizmo> callback : callbacks) {
			callback.accept(gizmo);
		}
	});
	public static final Event<BiConsumer<String, Schematic>> CHANGE_SCHEMATIC = EventFactory.createArrayBacked(BiConsumer.class, callbacks -> (name, schematic) -> {
		for (BiConsumer<String, Schematic> callback : callbacks) {
			callback.accept(name, schematic);
		}
	});
	public static final Event<Runnable> UPDATE_HISTORY = EventFactory.createArrayBacked(Runnable.class, callbacks -> () -> {
		for (Runnable callback : callbacks) {
			callback.run();
		}
	});
	public static final Event<Runnable> CHANGE_SELECTION = EventFactory.createArrayBacked(Runnable.class, callbacks -> () -> {
		for (Runnable callback : callbacks) {
			callback.run();
		}
	});

	private static Tool tool;
	private static List<Target> targets = new ArrayList<>();
	private static Set<BlockPos> positions = new HashSet<>();
	private static Gizmo selected = null;
	private static GizmoHandle activeHandle = null;
	private static Vec3d activeHandleOrigin = null;
	private static Set<Gizmo> gizmos = new HashSet<>();
	private static List<GizmoHandle> gizmoHandles = new ArrayList<>();
	private static Map<String, Schematic> schematics = new HashMap<>();
	private static List<EditStats> history = new ArrayList<>();
	private static int historyIndex = -1;
	private static long historySize = -1;
	private static float cameraSpeed = 2f;

	private static TransientToolState toolState;
	private static Persistant persistant;
	private static int ticks;

	public static Tool tool() {
		return tool;
	}

	public static void tool(Tool tool) {
		EditorState.tool = tool;
		Config toolConfig = persistant().active(tool);
		EditorState.targets.clear();
		EditorState.toolState = new TransientToolState();

		CHANGE_TOOL.invoker().accept(tool);
		CHANGE_TOOL_CONFIG.invoker().accept(toolConfig);
		CHANGE_TARGETS.invoker().accept(null);
	}

	public static Config toolConfig() {
		if(tool == null) return null;
		return persistant().active(tool);
	}

	public static void toolConfig(Config toolConfig) {
		if(tool == null) return;
		if(!tool.isValid(toolConfig)) return;

		toolConfig = new Config(new HashMap<>(toolConfig.values()));
		persistant().active(tool, toolConfig);
		CHANGE_TOOL_CONFIG.invoker().accept(toolConfig);
	}

	public static Configured<Tool> configuredTool() {
		if(tool == null) return null;
		Config toolConfig = persistant().active(tool);
		if(!tool.isValid(toolConfig)) return null;

		return new Configured<>(tool, toolConfig);
	}
	
	public static TransientToolState toolState() {
		return toolState;
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

	public static Configured<Filter> filter() {
		return persistant().filter();
	}

	public static void filter(Configured<Filter> filter) {
		persistant.filter(filter);
		CHANGE_FILTER.invoker().accept(filter);
	}
	
	public static Gizmo selected() {
		return selected;
	}
	
	public static void selected(Gizmo selected) {
		Gizmo previousSelected = EditorState.selected;
		addGizmo(selected);
		EditorState.selected = selected;
		EditorState.activeHandle = null;
		EditorState.gizmoHandles.clear();
		
		if(previousSelected != null) {
			previousSelected.deselected();
		}
		if(selected != null) {
			selected.selected();
			selected.addHandles(gizmoHandles);
		}
		
		CHANGE_SELECTED.invoker().accept(selected);
	}
	
	public static GizmoHandle activeHandle() {
		return activeHandle;
	}
	
	public static Vec3d activeHandleOrigin() {
		return activeHandleOrigin;
	}
	
	public static void activeHandle(GizmoHandle activeHandle, Vec3d activeHandleOrigin) {
		EditorState.activeHandle = activeHandle;
		EditorState.activeHandleOrigin = activeHandleOrigin;
	}
	
	public static List<GizmoHandle> gizmoHandles() {
		return Collections.unmodifiableList(gizmoHandles);
	}
	
	public static Set<Gizmo> gizmos() {
		return Collections.unmodifiableSet(gizmos);
	}
	
	public static void addGizmo(Gizmo gizmo) {
		if(gizmo == null) return;
		gizmos.add(gizmo);
	}
	
	public static void removeGizmo(Gizmo gizmo) {
		gizmos.remove(gizmo);
		if(selected == gizmo) {
			selected(null);
		}
	}

	public static Schematic schematic(String name) {
		return schematics.get(name);
	}

	public static void schematic(String name, Schematic schematic) {
		if(schematic == null) EditorState.schematics.remove(name);
		else EditorState.schematics.put(name, schematic);
		CHANGE_SCHEMATIC.invoker().accept(name, schematic);
	}
	
	public static Map<String, Schematic> schematics() {
		return Collections.unmodifiableMap(EditorState.schematics);
	}

	public static Context context() {
		return new Context(persistant().palette(), filter());
	}

	public static List<EditStats> history() {
		return Collections.unmodifiableList(history);
	}

	public static int historyIndex() {
		return historyIndex;
	}
	
	public static long historySize() {
		return historySize;
	}

	public static void history(List<EditStats> history, int historyIndex, boolean append, long historySize) {
		if(!append) EditorState.history.clear();
		EditorState.history.addAll(history);
		EditorState.historyIndex = historyIndex;
		EditorState.historySize = historySize;
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
		private Map<Identifier, Config> TOOL_ACTIVE = new HashMap<>();
		private Map<Identifier, Map<String, Config>> TOOL_PRESETS = new HashMap<>();

		private BlockPalette PALETTE_ACTIVE = new BlockPalette(BlockPalette.DEFAULT.getEntries());
		private Map<String, BlockPalette> PALETTE_PRESETS = new HashMap<>();
		
		private Selection selection;
		
		private Configured<Filter> filter = new Configured<>(VoxEdit.FILTER_BOOLEAN, VoxEdit.FILTER_BOOLEAN.getDefaultConfig());
		
		private boolean modified = false;

		private Persistant() {
			// load tools
			VoxEdit.TOOL_REGISTRY.forEach(tool -> {
				Identifier id = tool.id();

				Path dir = VoxEditClient.dataPath().resolve("tools/"+tool.id().getNamespace()+"/"+tool.id().getPath()+"/");
				Path current = dir.resolve("current.json");
				TOOL_ACTIVE.put(tool.id(), VoxEditUtil.readJson(current, Config.CODEC, tool.getDefaultConfig()));

				// load saved presets
				Map<String, Config> presets = new HashMap<>();
				try {
					if(Files.exists(dir.resolve("presets/"))) {
						Files.list(dir.resolve("presets/")).forEach(path -> {
							String fileName = path.getFileName().toString();
							if(!fileName.endsWith(".json")) return;
							String name = fileName.substring(0, fileName.length()-5);

							Config config = VoxEditUtil.readJson(path, Config.CODEC, null);
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
			
			// load filter
			filter = VoxEditUtil.readJson(VoxEditClient.dataPath().resolve("filter.json"), VECodecs.CONFIGURED_FILTER, new Configured<>(VoxEdit.FILTER_BOOLEAN, VoxEdit.FILTER_BOOLEAN.getDefaultConfig()));

			// TODO: other state
		}

		private void persistIfModified() {
			if(!modified) return;
			modified = false;

			// save tool state
			VoxEdit.TOOL_REGISTRY.forEach(tool -> {
				Path dir = VoxEditClient.dataPath().resolve("tools/"+tool.id().getNamespace()+"/"+tool.id().getPath()+"/");

				Path current = dir.resolve("current.json");
				VoxEditUtil.writeJson(current, Config.CODEC, active(tool));

				for(var e : presets(tool).entrySet()) {
					VoxEditUtil.writeJson(getPresetPath(tool, e.getKey()), Config.CODEC, e.getValue());
				}
			});

			// save palette state
			VoxEditUtil.writeJson(VoxEditClient.dataPath().resolve("palette.json"), BlockPalette.CODEC, PALETTE_ACTIVE);
			for(var e : PALETTE_PRESETS.entrySet()) {
				VoxEditUtil.writeJson(getPalettePath(e.getKey()), BlockPalette.CODEC, e.getValue());
			}
			
			// save filter state
			VoxEditUtil.writeJson(VoxEditClient.dataPath().resolve("filter.json"), VECodecs.CONFIGURED_FILTER, filter);
		}
		
		private Path getPresetPath(Tool tool, String name) {
			Path dir = VoxEditClient.dataPath().resolve("tools/"+tool.id().getNamespace()+"/"+tool.id().getPath()+"/");
			return dir.resolve("presets/"+name+".json");
		}
		
		private Path getPalettePath(String name) {
			return VoxEditClient.dataPath().resolve("palettes/"+name+".json");
		}

		public Map<String, Config> presets(Tool tool) {
			return Collections.unmodifiableMap(TOOL_PRESETS.get(tool.id()));
		}
		
		public void preset(Tool tool, String name, Config preset) {
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

		public Config active(Tool tool) {
			return TOOL_ACTIVE.computeIfAbsent(tool.id(), (id) -> tool.getDefaultConfig());
		}

		public void active(Tool tool, Config config) {
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
		
		public Selection selection() {
			return selection;
		}
		
		public void selection(Selection selection) {
			this.selection = selection;
			CHANGE_SELECTION.invoker().run();
		}
		
		public Configured<Filter> filter() {
			return filter;
		}
		
		public void filter(Configured<Filter> filter) {
			this.filter = filter;
			modified = true;
		}
	}
	
	public static class TransientToolState {
		private List<BlockPos> positions = new ArrayList<>();
		private Map<String, Boolean> flags = new HashMap<>();
		
		public List<BlockPos> positions() {
			return positions;
		}
		
		public boolean getFlag(String name) {
			return flags.getOrDefault(name, false);
		}
		
		public void setFlag(String name, boolean flag) {
			flags.put(name, flag);
		}
	}

	public static record ToolPreset(String name, Config config) {
	}
}
