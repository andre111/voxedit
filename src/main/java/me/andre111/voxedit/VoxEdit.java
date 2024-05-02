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
package me.andre111.voxedit;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.serialization.Lifecycle;

import me.andre111.voxedit.editor.action.EditAction;
import me.andre111.voxedit.editor.action.ModifyBlockEntityAction;
import me.andre111.voxedit.editor.action.ModifyEntityAction;
import me.andre111.voxedit.editor.action.SetBlockAction;
import me.andre111.voxedit.editor.history.EditHistoryReader;
import me.andre111.voxedit.editor.history.EditHistoryWriter;
import me.andre111.voxedit.network.ServerNetworking;
import me.andre111.voxedit.selection.SelectionAdd;
import me.andre111.voxedit.selection.SelectionBox;
import me.andre111.voxedit.selection.SelectionSet;
import me.andre111.voxedit.selection.SelectionShape;
import me.andre111.voxedit.selection.SelectionSubtract;
import me.andre111.voxedit.selection.SelectionType;
import me.andre111.voxedit.tool.Tool;
import me.andre111.voxedit.tool.ToolBlend;
import me.andre111.voxedit.tool.ToolBrush;
import me.andre111.voxedit.tool.EditNBTTool;
import me.andre111.voxedit.tool.ToolExtrude;
import me.andre111.voxedit.tool.ToolFill;
import me.andre111.voxedit.tool.ToolFlatten;
import me.andre111.voxedit.tool.ToolPaint;
import me.andre111.voxedit.tool.ToolPlace;
import me.andre111.voxedit.tool.ToolRaise;
import me.andre111.voxedit.tool.ToolScatter;
import me.andre111.voxedit.tool.ToolSmooth;
import me.andre111.voxedit.tool.shape.Cube;
import me.andre111.voxedit.tool.shape.Cylinder;
import me.andre111.voxedit.tool.shape.Disc;
import me.andre111.voxedit.tool.shape.HollowCube;
import me.andre111.voxedit.tool.shape.HollowSphere;
import me.andre111.voxedit.tool.shape.Shape;
import me.andre111.voxedit.tool.shape.Sphere;

public class VoxEdit implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("voxedit");
    
    // Note: Order is important
    // 1. registry keys
    public static final RegistryKey<Registry<EditAction.Type<?>>> ACTION_TYPE_REGISTRY_KEY = RegistryKey.ofRegistry(id("action_type"));
    public static final RegistryKey<Registry<Shape>> SHAPE_REGISTRY_KEY = RegistryKey.ofRegistry(id("shape"));
    public static final RegistryKey<Registry<Tool>> TOOL_REGISTRY_KEY = RegistryKey.ofRegistry(id("tool"));
    public static final RegistryKey<Registry<SelectionType<?>>> SELECTION_TYPE_REGISTRY_KEY = RegistryKey.ofRegistry(id("selection_type"));
    
    // 2. registries
    public static final Registry<EditAction.Type<?>> ACTION_TYPE_REGISTRY = new SimpleRegistry<>(ACTION_TYPE_REGISTRY_KEY, Lifecycle.stable());
    public static final Registry<Shape> SHAPE_REGISTRY = new SimpleRegistry<>(SHAPE_REGISTRY_KEY, Lifecycle.stable());
    public static final Registry<Tool> TOOL_REGISTRY = new SimpleRegistry<>(TOOL_REGISTRY_KEY, Lifecycle.stable());
    public static final Registry<SelectionType<?>> SELECTION_TYPE_REGISTRY = new SimpleRegistry<>(SELECTION_TYPE_REGISTRY_KEY, Lifecycle.stable());
    
    // 3. registry entries
    public static final EditAction.Type<SetBlockAction> ACTION_SET_BLOCK = registerAction(id("set_block"), SetBlockAction::write, SetBlockAction::read);
    public static final EditAction.Type<ModifyBlockEntityAction> ACTION_MODIFY_BLOCK_ENTITY = registerAction(id("modify_block_entity"), ModifyBlockEntityAction::write, ModifyBlockEntityAction::read);
    public static final EditAction.Type<ModifyEntityAction> ACTION_MODIFY_ENTITY = registerAction(id("modify_entity"), ModifyEntityAction::write, ModifyEntityAction::read);
    
    public static final Shape SHAPE_CUBE = Registry.register(SHAPE_REGISTRY, id("cube"), new Cube());
    public static final Shape SHAPE_SPHERE = Registry.register(SHAPE_REGISTRY, id("sphere"), new Sphere());
    public static final Shape SHAPE_DISC = Registry.register(SHAPE_REGISTRY, id("disc"), new Disc());
    public static final Shape SHAPE_CYLINDER = Registry.register(SHAPE_REGISTRY, id("cylinder"), new Cylinder());
    public static final Shape SHAPE_HOLLOW_CUBE = Registry.register(SHAPE_REGISTRY, id("hollow_cube"), new HollowCube());
    public static final Shape SHAPE_HOLLOW_SPHERE = Registry.register(SHAPE_REGISTRY, id("hollow_sphere"), new HollowSphere());
    
    public static final ToolBrush TOOL_BRUSH = Registry.register(TOOL_REGISTRY, id("brush"), new ToolBrush());
    public static final ToolPaint TOOL_PAINT = Registry.register(TOOL_REGISTRY, id("paint"), new ToolPaint());
    public static final ToolScatter TOOL_SCATTER = Registry.register(TOOL_REGISTRY, id("scatter"), new ToolScatter());
    public static final ToolSmooth TOOL_SMOOTH = Registry.register(TOOL_REGISTRY, id("smooth"), new ToolSmooth());
    public static final ToolFill TOOL_FILL = Registry.register(TOOL_REGISTRY, id("fill"), new ToolFill());
    public static final ToolFlatten TOOL_FLATTEN = Registry.register(TOOL_REGISTRY, id("flatten"), new ToolFlatten());
    public static final ToolBlend TOOL_BLEND = Registry.register(TOOL_REGISTRY, id("blend"), new ToolBlend());
    public static final ToolPlace TOOL_PLACE = Registry.register(TOOL_REGISTRY, id("place"), new ToolPlace());
    public static final ToolExtrude TOOL_EXTRUDE = Registry.register(TOOL_REGISTRY, id("extrude"), new ToolExtrude());
    public static final ToolRaise TOOL_RAISE = Registry.register(TOOL_REGISTRY, id("raise"), new ToolRaise());
    public static final EditNBTTool TOOL_EDITNBT = Registry.register(TOOL_REGISTRY, id("nbtedit"), new EditNBTTool());
    
    public static final SelectionType<SelectionBox> SEL_BOX = Registry.register(SELECTION_TYPE_REGISTRY, id("box"), SelectionType.of(SelectionBox.CODEC));
    public static final SelectionType<SelectionShape> SEL_SHAPE = Registry.register(SELECTION_TYPE_REGISTRY, id("shape"), SelectionType.of(SelectionShape.CODEC));
    public static final SelectionType<SelectionSet> SEL_SET = Registry.register(SELECTION_TYPE_REGISTRY, id("set"), SelectionType.of(SelectionSet.CODEC));
    public static final SelectionType<SelectionAdd> SEL_ADD = Registry.register(SELECTION_TYPE_REGISTRY, id("add"), SelectionType.of(SelectionAdd.CODEC));
    public static final SelectionType<SelectionSubtract> SEL_SUBTRACT = Registry.register(SELECTION_TYPE_REGISTRY, id("subtract"), SelectionType.of(SelectionSubtract.CODEC));
    
    public static final int MAX_TARGETS = 1024;
    public static final int PREVIEW_DELAY = 5;
    
	@Override
	public void onInitialize() {
    	ServerNetworking.init();
    	//TODO: Remove general tick freezing (only when editor is active? and provide option to disable)
		ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
    		server.getTickManager().setFrozen(true);
    	});
	}
	
	public static Identifier id(String path) {
		return new Identifier("voxedit", path);
	}
	
	public static <A extends EditAction<A>> EditAction.Type<A> registerAction(Identifier id, BiConsumer<A, EditHistoryWriter> writer, Function<EditHistoryReader, A> reader) {
		EditAction.Type<A> type = EditAction.Type.create(id, writer, reader);
		Registry.register(ACTION_TYPE_REGISTRY, id, type);
		return type;
	}
	
	public static Path dataPath(MinecraftServer server) {
		Path path = server.getSavePath(WorldSavePath.ROOT).resolve("voxedit/server/");
		try {
			if(!Files.exists(path)) Files.createDirectories(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return path;
	}
}
