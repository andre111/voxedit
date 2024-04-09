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
package me.andre111.voxedit.client;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.client.gui.screen.EditorScreen;
import me.andre111.voxedit.client.network.ClientNetworking;
import me.andre111.voxedit.client.renderer.HudRenderer;
import me.andre111.voxedit.client.renderer.SelectionRenderer;
import me.andre111.voxedit.client.renderer.TargetRenderer;
import me.andre111.voxedit.client.renderer.SchematicRenderer;
import me.andre111.voxedit.item.VoxEditItem;
import me.andre111.voxedit.network.Command;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

@Environment(value=EnvType.CLIENT)
public class VoxEditClient implements ClientModInitializer {
	public static final KeyBinding INCREASE_RADIUS = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.increaseRadius", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_PAGE_UP, "key.category.voxedit"));
    public static final KeyBinding DECREASE_RADIUS = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.decreaseRadius", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_PAGE_DOWN, "key.category.voxedit"));
    public static final KeyBinding INCREASE_SPEED = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.increaseSpeed", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_KP_ADD, "key.category.voxedit"));
    public static final KeyBinding DECREASE_SPEED = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.decreaseSpeed", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_KP_SUBTRACT, "key.category.voxedit"));
    public static final KeyBinding UNDO = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.undo", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.category.voxedit"));
    public static final KeyBinding REDO = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.redo", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Y, "key.category.voxedit"));
    public static final KeyBinding OPEN_MENU = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.openMenu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.category.voxedit"));
	
	public static SchematicRenderer testRenderer;
	public static Matrix4f modelViewMat;
	public static Matrix4f projectionMat;
	
	public static SchematicRenderer previewRenderer;
	
	@Override
	public void onInitializeClient() {
		ClientNetworking.init();
		HudRenderer.init();
		
		ItemGroupEvents.MODIFY_ENTRIES_ALL.register((group, entries) -> {
			if(group.getType() == ItemGroup.Type.CATEGORY && entries.shouldShowOpRestrictedItems()) {
				boolean hasCB = entries.getDisplayStacks().stream().filter(stack -> stack.getItem() == Items.COMMAND_BLOCK).findAny().isPresent();
				if(hasCB) {
					entries.add(VoxEdit.ITEM_EDITOR.getDefaultStack());
					entries.add(VoxEdit.ITEM_SELECT.getDefaultStack());
				}
			}
		});
		
		ClientPlayConnectionEvents.INIT.register((handler, client) -> {
			ClientStates.recreateInstance(handler.getRegistryManager());
		});
    	ClientTickEvents.START_CLIENT_TICK.register((client) -> {
    		if(client.world != null && client.player != null) tick();
    	});
    	WorldRenderEvents.LAST.register(VoxEditClient::render);
    	ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> {
    		ItemStack stack = player.getMainHandStack();
    		if(stack.getItem() instanceof VoxEditItem) {
    			if(player.isCreative() && MinecraftClient.getInstance().attackCooldown <= 0) {
    				MinecraftClient.getInstance().attackCooldown = 5;
    				ClientNetworking.sendCommand(Command.LEFT_CLICK);
    			}
    			return true;
    		}
    		return false;
    	});
	}
	
	public static void tick() {
		if(INCREASE_SPEED.wasPressed()) {
			ClientStates.instance().setCameraSpeed(Math.min(ClientStates.instance().getCameraSpeed()+1, 10f));
			MinecraftClient.getInstance().getMessageHandler().onGameMessage(Text.translatable("voxedit.feedback.cameraSpeed", ClientStates.instance().getCameraSpeed()), true);
		}
		if(DECREASE_SPEED.wasPressed()) {
			ClientStates.instance().setCameraSpeed(Math.max(1f, ClientStates.instance().getCameraSpeed()-1));
			MinecraftClient.getInstance().getMessageHandler().onGameMessage(Text.translatable("voxedit.feedback.cameraSpeed", ClientStates.instance().getCameraSpeed()), true);
		}
		//TODO: remove test stuff
		if(OPEN_MENU.wasPressed()) {
			if(MinecraftClient.getInstance().player.getMainHandStack().isOf(VoxEdit.ITEM_SELECT)) {
				ClientNetworking.sendCommand(Command.DEV);
			} else {
				MinecraftClient.getInstance().setScreen(EditorScreen.get());
				EditorScreen.get().rebuild();
			}
		}
		
		while(UNDO.wasPressed()) {
			if(Screen.hasControlDown()) ClientNetworking.sendCommand(Command.UNDO);
		}
		while(REDO.wasPressed()) {
			if(Screen.hasControlDown()) ClientNetworking.sendCommand(Command.REDO);
		}
		if(!Screen.hasAltDown() && EditorScreen.get().isActive() && MinecraftClient.getInstance().currentScreen != EditorScreen.get()) {
			MinecraftClient.getInstance().getWindow().setScaleFactor(1);
			MinecraftClient.getInstance().setScreen(EditorScreen.get());
			restoreGuiScale();
		}
	}
	
	public static void render(WorldRenderContext context) {
		modelViewMat = context.positionMatrix();
		projectionMat = context.projectionMatrix();
		
		if(!EditorState.targets().isEmpty() && previewRenderer == null) {
            TargetRenderer.render(ClientStates.instance().getPositions(), context);
		}
		SelectionRenderer.render(ClientStates.instance().getSelection(), context);
		
		HitResult result = MinecraftClient.getInstance().player.raycast(64, 0, true);
		if(testRenderer != null && result instanceof BlockHitResult blockHit) {
			BlockPos origin = blockHit.getBlockPos().offset(blockHit.getSide());
			testRenderer.draw(origin, context.camera(), context.frustum(), context.positionMatrix(), context.projectionMatrix(), false);
		}
		
		if(previewRenderer != null) {
			previewRenderer.draw(Vec3i.ZERO, context.camera(), context.frustum(), context.positionMatrix(), context.projectionMatrix(), true);
		}
	}
	
	public static void restoreGuiScale() {
		MinecraftClient mc = MinecraftClient.getInstance();
		mc.getWindow().setScaleFactor(mc.getWindow().calculateScaleFactor(mc.options.getGuiScale().getValue(), mc.forcesUnicodeFont()));
	}
}
