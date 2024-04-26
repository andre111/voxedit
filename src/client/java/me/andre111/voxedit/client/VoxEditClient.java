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

import org.lwjgl.glfw.GLFW;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.client.gui.screen.EditorScreen;
import me.andre111.voxedit.client.network.ClientNetworking;
import me.andre111.voxedit.client.renderer.HudRenderer;
import me.andre111.voxedit.client.tool.ObjectTool;
import me.andre111.voxedit.client.tool.SelectTool;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.registry.Registry;

@Environment(value=EnvType.CLIENT)
public class VoxEditClient implements ClientModInitializer {
    public static final KeyBinding INCREASE_RADIUS = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.increaseRadius", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_PAGE_UP, "key.category.voxedit"));
    public static final KeyBinding DECREASE_RADIUS = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.decreaseRadius", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_PAGE_DOWN, "key.category.voxedit"));
    public static final KeyBinding INCREASE_SPEED = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.increaseSpeed", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_KP_ADD, "key.category.voxedit"));
    public static final KeyBinding DECREASE_SPEED = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.decreaseSpeed", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_KP_SUBTRACT, "key.category.voxedit"));
    public static final KeyBinding UNDO = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.undo", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.category.voxedit"));
    public static final KeyBinding REDO = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.redo", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Y, "key.category.voxedit"));
    public static final KeyBinding OPEN_MENU = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.openMenu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.category.voxedit"));
	
    public static final SelectTool TOOL_SELECT = Registry.register(VoxEdit.TOOL_REGISTRY, VoxEdit.id("select"), new SelectTool());
    public static final ObjectTool TOOL_OBJECT = Registry.register(VoxEdit.TOOL_REGISTRY, VoxEdit.id("object"), new ObjectTool());
    
	@Override
	public void onInitializeClient() {
		ClientNetworking.init();
		HudRenderer.init();
		
    	ClientTickEvents.START_CLIENT_TICK.register((client) -> {
    		if(client.world != null && client.player != null) tick();
    	});
	}
	
	public static void tick() {
		if(OPEN_MENU.wasPressed()) {
			unscaleGui();
			MinecraftClient.getInstance().setScreen(EditorScreen.get());
			restoreGuiScale();
		}
		
		if(!Screen.hasAltDown() && EditorScreen.get().isActive() && MinecraftClient.getInstance().currentScreen == null) {
			unscaleGui();
			MinecraftClient.getInstance().setScreen(EditorScreen.get());
			restoreGuiScale();
		}
		
		EditorState.tick();
	}
	
	public static void unscaleGui() {
		MinecraftClient mc = MinecraftClient.getInstance();
		mc.getWindow().setScaleFactor(1);
	}
	
	public static void restoreGuiScale() {
		MinecraftClient mc = MinecraftClient.getInstance();
		mc.getWindow().setScaleFactor(mc.getWindow().calculateScaleFactor(mc.options.getGuiScale().getValue(), mc.forcesUnicodeFont()));
	}
	
	public static Path dataPath() {
		Path path = MinecraftClient.getInstance().runDirectory.toPath().resolve("voxedit/client/");
		try {
			if(!Files.exists(path)) Files.createDirectories(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return path;
	}
}
