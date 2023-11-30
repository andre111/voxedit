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

import org.lwjgl.glfw.GLFW;
import org.spongepowered.include.com.google.common.base.Objects;

import me.andre111.voxedit.client.ClientState;
import me.andre111.voxedit.client.gui.screen.ToolSelectionScreen;
import me.andre111.voxedit.client.network.ClientNetworking;
import me.andre111.voxedit.client.renderer.EditorRenderer;
import me.andre111.voxedit.client.renderer.HudRenderer;
import me.andre111.voxedit.client.renderer.SelectionRenderer;
import me.andre111.voxedit.client.renderer.ToolRenderer;
import me.andre111.voxedit.item.ToolItem;
import me.andre111.voxedit.item.VoxEditItem;
import me.andre111.voxedit.network.Command;
import me.andre111.voxedit.tool.ConfiguredTool;
import me.andre111.voxedit.tool.Tool;
import me.andre111.voxedit.tool.config.ToolConfig;
import me.andre111.voxedit.tool.data.Selection;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;

@Environment(value=EnvType.CLIENT)
public class VoxEditClient implements ClientModInitializer {
	public static final KeyBinding INCREASE_RADIUS = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.increaseRadius", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_PAGE_UP, "key.category.voxedit"));
    public static final KeyBinding DECREASE_RADIUS = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.decreaseRadius", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_PAGE_DOWN, "key.category.voxedit"));
    public static final KeyBinding INCREASE_SPEED = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.increaseSpeed", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_KP_ADD, "key.category.voxedit"));
    public static final KeyBinding DECREASE_SPEED = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.decreaseSpeed", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_KP_SUBTRACT, "key.category.voxedit"));
    public static final KeyBinding UNDO = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.undo", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.category.voxedit"));
    public static final KeyBinding REDO = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.redo", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Y, "key.category.voxedit"));
    public static final KeyBinding OPEN_MENU = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.openMenu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.category.voxedit"));

	public static int retargetCooldown;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void onInitializeClient() {
		ClientNetworking.init();
		BuiltinItemRendererRegistry.INSTANCE.register(VoxEdit.ITEM_TOOL, ToolRenderer.INSTANCE);
		BuiltinItemRendererRegistry.INSTANCE.register(VoxEdit.ITEM_EDITOR, EditorRenderer.INSTANCE);
		HudRenderer.init();
		
		ItemGroupEvents.MODIFY_ENTRIES_ALL.register((group, entries) -> {
			if(group.getType() == ItemGroup.Type.CATEGORY && entries.shouldShowOpRestrictedItems()) {
				boolean hasCB = entries.getDisplayStacks().stream().filter(stack -> stack.getItem() == Items.COMMAND_BLOCK).findAny().isPresent();
				if(hasCB) {
					for(Tool<?, ?> tool : VoxEdit.TOOL_REGISTRY) {
						entries.add(VoxEdit.ITEM_TOOL.getStackWith(tool.getDefault()));
						for(ToolConfig config : tool.getAdditionalCreativeMenuConfigs()) {
							entries.add(VoxEdit.ITEM_TOOL.getStackWith(new ConfiguredTool(tool, config)));
						}
					}
					entries.add(Presets.andre111Stack);
					entries.add(VoxEdit.ITEM_EDITOR.getDefaultStack());
				}
			}
		});
		
    	ClientTickEvents.START_CLIENT_TICK.register((mc) -> {
    		if(mc.world != null && mc.player != null) tick();
    	});
    	WorldRenderEvents.LAST.register((context) -> {
    		render(context.matrixStack(), context.tickDelta());
    	});
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
		if(retargetCooldown > 0) retargetCooldown--;
		
		ClientState.player = MinecraftClient.getInstance().player;
		
		ToolItem.Data oldActive = ClientState.active;
		BlockHitResult oldTarget = ClientState.target;
		ClientState.active = null;
		ItemStack stack = ClientState.player.getMainHandStack();
		if(stack.getItem() instanceof ToolItem toolItem) {
			ClientState.active = ToolItem.readToolData(stack);
		}
		
		if(ClientState.active != null) {
			if(oldActive == null || !ClientState.active.selected().equals(oldActive.selected())) { 
				HudRenderer.getToolSettingsScreen().rebuild(); 
				ClientState.positions = null; 
			}
			if(MinecraftClient.getInstance().currentScreen != null) return;
			
			ClientState.target = Selection.getTargetOf(ClientState.player, ClientState.active.selected().config());
			if(oldTarget == null || !Objects.equal(ClientState.target.getBlockPos(), oldTarget.getBlockPos()) || !Objects.equal(ClientState.target.getSide(), oldTarget.getSide())) {
				ClientState.positions = null;
			}
			
			if(INCREASE_RADIUS.wasPressed()) {
				ClientNetworking.setSelectedConfig(ClientState.active.selected().config().withRadius(Math.min(ClientState.active.selected().config().radius()+1, 16)));
			}
			if(DECREASE_RADIUS.wasPressed()) {
				ClientNetworking.setSelectedConfig(ClientState.active.selected().config().withRadius(Math.max(1, ClientState.active.selected().config().radius()-1)));
			}
			if(INCREASE_SPEED.wasPressed()) {
				ClientState.cameraSpeed = Math.min(ClientState.cameraSpeed+1, 10f);
				MinecraftClient.getInstance().getMessageHandler().onGameMessage(Text.translatable("voxedit.feedback.cameraSpeed", ClientState.cameraSpeed), true);
			}
			if(DECREASE_SPEED.wasPressed()) {
				ClientState.cameraSpeed = Math.max(1f, ClientState.cameraSpeed-1);
				MinecraftClient.getInstance().getMessageHandler().onGameMessage(Text.translatable("voxedit.feedback.cameraSpeed", ClientState.cameraSpeed), true);
			}
			if(OPEN_MENU.wasPressed()) {
				if(Screen.hasControlDown()) MinecraftClient.getInstance().setScreen(new ToolSelectionScreen(ClientState.active));
				else MinecraftClient.getInstance().setScreen(HudRenderer.getToolSettingsScreen());
			}
		}
		
		while(UNDO.wasPressed()) {
			if(Screen.hasControlDown()) ClientNetworking.sendCommand(Command.UNDO);
		}
		while(REDO.wasPressed()) {
			if(Screen.hasControlDown()) ClientNetworking.sendCommand(Command.REDO);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void render(MatrixStack matrices, float frame) {
		if(ClientState.active != null && ClientState.target != null) {
			if(retargetCooldown == 0 || ClientState.positions == null) {
				retargetCooldown = 200;
				ClientState.positions = ((Tool) ClientState.active.selected().tool()).getBlockPositions(MinecraftClient.getInstance().world, ClientState.target, ClientState.active.selected().config());
			}
            
            SelectionRenderer.render(ClientState.positions, matrices, frame);
		}
	}
}
