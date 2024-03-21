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
package me.andre111.voxedit;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.component.DataComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.serialization.Lifecycle;

import me.andre111.voxedit.item.EditorItem;
import me.andre111.voxedit.item.SelectItem;
import me.andre111.voxedit.item.ToolItem;
import me.andre111.voxedit.item.ToolItem.Data;
import me.andre111.voxedit.network.ServerNetworking;
import me.andre111.voxedit.tool.ConfiguredTool;
import me.andre111.voxedit.tool.Tool;
import me.andre111.voxedit.tool.ToolBlend;
import me.andre111.voxedit.tool.ToolBrush;
import me.andre111.voxedit.tool.ToolFill;
import me.andre111.voxedit.tool.ToolFlatten;
import me.andre111.voxedit.tool.ToolPlace;
import me.andre111.voxedit.tool.ToolSmooth;
import me.andre111.voxedit.tool.config.ToolConfigBrush;

public class VoxEdit implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("voxedit");
    
    public static final RegistryKey<Registry<Tool<?, ?>>> TOOL_REGISTRY_KEY = RegistryKey.ofRegistry(id("tool"));
    public static final Registry<Tool<?, ?>> TOOL_REGISTRY = new SimpleRegistry<Tool<?, ?>>(TOOL_REGISTRY_KEY, Lifecycle.stable());
    
    public static final ToolBrush TOOL_BRUSH = Registry.register(TOOL_REGISTRY, id("brush"), new ToolBrush());
    public static final ToolSmooth TOOL_SMOOTH = Registry.register(TOOL_REGISTRY, id("smooth"), new ToolSmooth());
    public static final ToolFill TOOL_FILL = Registry.register(TOOL_REGISTRY, id("fill"), new ToolFill());
    public static final ToolFlatten TOOL_FLATTEN = Registry.register(TOOL_REGISTRY, id("flatten"), new ToolFlatten());
    public static final ToolBlend TOOL_BLEND = Registry.register(TOOL_REGISTRY, id("blend"), new ToolBlend());
    public static final ToolPlace TOOL_PLACE = Registry.register(TOOL_REGISTRY, id("place"), new ToolPlace());
    
	public static final ConfiguredTool<?, ?> DEFAULT_TOOL = new ConfiguredTool<>(TOOL_BRUSH, new ToolConfigBrush());
    
    public static final ToolItem ITEM_TOOL = Registry.register(Registries.ITEM, id("tool"), new ToolItem());
    public static final EditorItem ITEM_EDITOR = Registry.register(Registries.ITEM, id("editor"), new EditorItem());
    public static final SelectItem ITEM_SELECT = Registry.register(Registries.ITEM, id("select"), new SelectItem());
    
    public static final DataComponentType<Data> DATA_COMPONENT = Registry.register(Registries.DATA_COMPONENT_TYPE, id("tool"), DataComponentType.<Data>builder().codec(Data.CODEC).build());
    
	@Override
	public void onInitialize() {
    	ServerNetworking.init();
		ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
    		server.getTickManager().setFrozen(true);
    	});
	}
	
	public static Identifier id(String path) {
		return new Identifier("voxedit", path);
	}
}
