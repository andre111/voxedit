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
package me.andre111.voxedit.client.gui.screen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.systems.RenderSystem;

import me.andre111.voxedit.client.EditorState;
import me.andre111.voxedit.client.gui.Textures;
import me.andre111.voxedit.client.gui.widget.AutoLayoutContainerWidget;
import me.andre111.voxedit.client.gui.widget.DropdownListWidget;
import me.andre111.voxedit.client.gui.widget.IntFieldWidget;
import me.andre111.voxedit.client.gui.widget.OverlayWidget;
import me.andre111.voxedit.client.gui.widget.RegistryEntryWidget;
import me.andre111.voxedit.client.gui.widget.json.JsonEditWidget;
import me.andre111.voxedit.client.network.ClientNetworking;
import me.andre111.voxedit.client.renderer.SchematicRenderer;
import me.andre111.voxedit.client.renderer.SchematicView;
import me.andre111.voxedit.data.jsondef.JsonDef;
import me.andre111.voxedit.data.jsondef.JsonDefLoader;
import me.andre111.voxedit.network.CPGenerateExample;
import me.andre111.voxedit.schematic.Schematic;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class FeatureEditorScreen extends UnscaledScreen {
	private int leftWidth = 300;
	private JsonElement jsonData;
	private Schematic schematic;
	private SchematicRenderer renderer;
	
	private float cameraDistance;
	private float cameraPitch;
	private float cameraYaw;
	private Quaternionf cameraRot;

    private OverlayWidget overlay;
	private AutoLayoutContainerWidget panel;
	private AutoLayoutContainerWidget featureConfigPanel;
	private RegistryEntryWidget<?> featureSelector;
	private DropdownListWidget featureTypeSelector;
	private JsonEditWidget<?> rootConfig;
	
	private long seed = 0;

	public FeatureEditorScreen(Screen parent) {
		super(parent, Text.translatable("voxedit.screen.feature"));
		
		cameraDistance = 16;
		cameraRot = new Quaternionf();
		
		setCameraRotation(-45, 0);
		
		overlay = new OverlayWidget(0, 0, width, height);
		addDrawableChild(overlay);
		
		int topHeight = 100;
		panel = new AutoLayoutContainerWidget(this, 0, 0, leftWidth, topHeight, Text.empty());
		{
			panel.addChild(featureSelector = RegistryEntryWidget.serverRetrieved(MinecraftClient.getInstance().textRenderer, 0, 0, leftWidth-50, 20, RegistryKeys.CONFIGURED_FEATURE, Identifier.ofVanilla("acacia"), id -> {}));
			panel.addChild(ButtonWidget.builder(Text.translatable("voxedit.load"), (button) -> {
				Identifier id = featureSelector.getValue();
				
				ClientNetworking.getConfiguredFeature(id).thenAccept(json -> {
					if(json != null && !json.isBlank()) {
						jsonData = (new Gson()).fromJson(json, JsonElement.class);
						ClientPlayNetworking.send(new CPGenerateExample(json, seed));
					}
				});
			}).dimensions(0, 0, 50, 20).build());
			panel.addChild(new TextWidget(50, 20, Text.translatable("voxedit.seed"), MinecraftClient.getInstance().textRenderer));
			panel.addChild(new IntFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, leftWidth-50, 20, Text.translatable("voxedit.seed"), 0, newSeed -> {
				if(newSeed != null && newSeed != seed) {
					seed = newSeed;
					if(jsonData != null) {
						ClientPlayNetworking.send(new CPGenerateExample((new Gson()).toJson(jsonData), seed));
					}
				}
			}));
			
			List<String> featureTypes = Registries.FEATURE.getIds().stream().map(Identifier::toString).toList();
			featureTypes = new ArrayList<>(featureTypes);
			featureTypes.sort(Comparator.naturalOrder());
			panel.addChild(featureTypeSelector = new DropdownListWidget(0, 0, leftWidth, 20, Text.translatable("voxedit.feature.type"), "", featureTypes, featureType -> {
				rebuild();
			}, overlay));
		}
		addDrawableChild(panel);
		panel.refreshPositions();
		addDrawableChild(featureConfigPanel = new AutoLayoutContainerWidget(this, 0, topHeight, leftWidth, height-topHeight, Text.empty()));
		
		EditorState.CHANGE_SCHEMATIC.register((id, schematic) -> {
			if(id.equals("voxedit.example")) {
				setSchematic(schematic);
			}
		});
	}
	
	private void setSchematic(Schematic schematic) {
		this.schematic = schematic;
		
		if(renderer != null) {
			renderer.close();
			renderer = null;
		}
		if(schematic != null) {
			renderer = new SchematicRenderer(new SchematicView(BlockPos.ORIGIN, schematic));
			cameraDistance = (float) Math.sqrt(schematic.getSizeX()*schematic.getSizeX() + schematic.getSizeY()*schematic.getSizeY() + schematic.getSizeZ()*schematic.getSizeZ()) + 1;
		}
	}
	
	private void setCameraRotation(float yaw, float pitch) {
		cameraPitch = pitch;
		cameraYaw = yaw;
		cameraRot.rotationYXZ((float)Math.PI - yaw * ((float)Math.PI / 180), -pitch * ((float)Math.PI / 180), 0.0f);
    }
	
	private void rebuild() {
		featureConfigPanel.children().clear();
		
		String type = featureTypeSelector.getValue();
		JsonDef def = JsonDefLoader.getDef("feature", Identifier.tryParse(type));
		if(def != null) {
			rootConfig = JsonEditWidget.create(def, "", featureConfigPanel, overlay);
			featureConfigPanel.addChild(rootConfig);
		}
		
		featureConfigPanel.refreshPositions();
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
        
        setCameraRotation(cameraYaw - (float) deltaX / 10, cameraPitch + (float) deltaY / 10);
        
        return true;
    }
	
	@Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        panel.render(context, mouseX, mouseY, delta);
        featureConfigPanel.render(context, mouseX, mouseY, delta);
        overlay.render(context, mouseX, mouseY, delta);
    }
	
	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) { 
		applyBlur(delta);
		renderDarkening(context);
		
		// preview
		if(renderer != null) {
            Matrix4f modelViewMat = new Matrix4f()
            		.translate(0, 0, -cameraDistance)
            		.translate(0, 0, schematic.getSizeZ() / 2f)
            		.rotate(cameraRot)
            		.translate(-schematic.getSizeX() / 2f, -schematic.getSizeY() / 2f, -schematic.getSizeZ() / 2f);
            
            float fov = 70;
            Matrix4f projMat = new Matrix4f().perspective(fov * 0.01745329238474369f, width / (float) height, 0.05f, 32 * 4f);
			
			renderer.draw(Vec3i.ZERO, Vec3d.ZERO, null, modelViewMat, projMat, false);
		}
    
		// gui
		RenderSystem.enableBlend();
		for(int i=0; i<2; i++) {
			context.drawTexture(Textures.BACKGROUND, 0, 0, 0, 0, leftWidth+1, height);
		}
		RenderSystem.disableBlend();
		
		context.drawVerticalLine(leftWidth+1, 0, height, 0xFFFFFFFF);
	}
}
