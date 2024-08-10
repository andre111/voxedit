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
package me.andre111.voxedit.client.gui.widget.editor;

import java.util.ArrayList;
import java.util.List;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.client.EditorState;
import me.andre111.voxedit.client.VoxEditClient;
import me.andre111.voxedit.client.gizmo.SchematicPlacement;
import me.andre111.voxedit.client.gui.Textures;
import me.andre111.voxedit.client.gui.screen.InputScreen;
import me.andre111.voxedit.client.gui.widget.ModListWidget;
import me.andre111.voxedit.client.network.ClientNetworking;
import me.andre111.voxedit.client.renderer.SchematicRenderer;
import me.andre111.voxedit.client.tool.ObjectTool;
import me.andre111.voxedit.network.Command;
import me.andre111.voxedit.schematic.Schematic;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class EditorPanelSchematics extends EditorPanel {
	private static final int PREVIEW_RESOLUTION = 256;
	private SchematicListWidget schematicList;
	private ButtonWidget placeButton;
	private ButtonWidget deleteButton;

	public EditorPanelSchematics(EditorWidget parent) {
		super(parent, VoxEdit.id("schematics"), Text.translatable("voxedit.screen.panel.schematics"));

		schematicList = new SchematicListWidget();
		placeButton = ButtonWidget.builder(Text.translatable("voxedit.schematic.place"), (b) -> {
			var selected = schematicList.getSelectedOrNull();
			if(selected == null) return;
			
			EditorState.selected(new SchematicPlacement(selected.name, BlockPos.ORIGIN));
			EditorState.tool(VoxEditClient.TOOL_OBJECT);
			EditorState.toolConfig(EditorState.toolConfig().with(ObjectTool.MODE, ObjectTool.Mode.POSITION));
		}).build();
		deleteButton = ButtonWidget.builder(Text.translatable("voxedit.schematic.delete"), (b) -> {
			var selected = schematicList.getSelectedOrNull();
			if(selected == null) return;
			
			InputScreen.showConfirmation(parent.getScreen(), Text.translatable("voxedit.prompt.schematic.delete", selected.name), () -> {
				ClientNetworking.sendCommand(Command.DELETE_SCHEMATIC, selected.name);
			});
		}).build();
		
		addContent(schematicList);
		addContent(placeButton);
		addContent(deleteButton);
		updateButtons();
		
		EditorState.CHANGE_SCHEMATIC.register((id, schematic) -> {
			schematicList.updateEntries();
			updateButtons();
		});
	}

    private void updateButtons() {
    	placeButton.active = schematicList != null && schematicList.getSelectedOrNull() != null;
    	deleteButton.active = schematicList != null && schematicList.getSelectedOrNull() != null;
    }
	
	@Override
	public void refreshPositions() {
		schematicList.setWidth(width);
		placeButton.setWidth((width-gapX)/2);
		deleteButton.setWidth((width-gapX)/2);
		super.refreshPositions();
	}

	@Environment(value=EnvType.CLIENT)
	class SchematicListWidget extends ModListWidget<SchematicListWidget.SchematicEntry> {
		public SchematicListWidget() {
			super(MinecraftClient.getInstance(), EditorPanelSchematics.this.width, 400, 20, 6);
			updateEntries();
		}

		public void updateEntries() {
			int i = selectedIndex();
			clearEntries();
			
			for(var e : EditorState.schematics().entrySet()) {
				if(!e.getValue().getInfo().visible()) continue;
				addEntry(new SchematicEntry(e.getKey(), e.getValue()));
			}
			
			List<SchematicEntry> list = children();
			if (i >= 0 && i < list.size()) {
				setSelected(i);
			}
		}

		@Override
		public void setSelected(int selectedIndex) {
			super.setSelected(selectedIndex);
			updateButtons();
		}

		@Environment(value=EnvType.CLIENT)
		class SchematicEntry extends ModListWidget.Entry<SchematicEntry> {
			private List<Element> children = new ArrayList<>();
			
			private final String name;
			
			private Identifier previewID;
			
			private final TextWidget nameWidget;
			private final TextWidget dimensionsWidget;

			private SchematicEntry(String name, Schematic schematic) {
				this.name = name;
				
				this.nameWidget = new TextWidget(Text.of(name), MinecraftClient.getInstance().textRenderer);
				this.dimensionsWidget = new TextWidget(Text.of(schematic.getSizeX()+" x "+schematic.getSizeY()+" x "+schematic.getSizeZ()), MinecraftClient.getInstance().textRenderer);
				
				children.add(nameWidget);
				children.add(dimensionsWidget);
				
				SchematicRenderer.getPreview(schematic, PREVIEW_RESOLUTION, PREVIEW_RESOLUTION, true).thenAccept(id -> previewID = id);
			}

			@Override
			public int getHeight() {
				return 80;
			}

			@Override
			public void positionChildren() {
				nameWidget.setPosition(getX()+8, getY()+8);
				dimensionsWidget.setPosition(getX()+8, getY()+8+12+8);
			}

			@Override
			protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
				context.drawGuiTexture(Textures.BUTTON.enabled(), getX(), getY(), getWidth(), getHeight()-4);
				nameWidget.render(context, mouseX, mouseY, delta);
				dimensionsWidget.render(context, mouseX, mouseY, delta);
				if(previewID != null) context.drawTexture(previewID, getX()+getWidth()-64-6, getY()+6, 64, 64, 0, 0, PREVIEW_RESOLUTION, PREVIEW_RESOLUTION, PREVIEW_RESOLUTION, PREVIEW_RESOLUTION);
			}

			@Override
			protected void appendClickableNarrations(NarrationMessageBuilder builder) {
			}

			@Override
			public List<? extends Element> children() {
				return children;
			}
		}
	}
}
