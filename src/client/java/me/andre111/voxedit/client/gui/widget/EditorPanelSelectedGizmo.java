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
package me.andre111.voxedit.client.gui.widget;

import java.util.function.Consumer;
import java.util.function.Supplier;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.client.EditorState;
import me.andre111.voxedit.client.gizmo.Gizmo;
import me.andre111.voxedit.client.gizmo.GizmoActions;
import me.andre111.voxedit.client.gizmo.Positionable;
import me.andre111.voxedit.client.gizmo.Rotatable90Deg;
import me.andre111.voxedit.client.gizmo.Sizeable;
import me.andre111.voxedit.tool.data.ToolConfig;
import me.andre111.voxedit.tool.data.ToolSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;

public class EditorPanelSelectedGizmo extends EditorPanel {
	private boolean rebuilding = false;
	private boolean reloading = false;
	
	private IntFieldWidget x, y, z;
	private IntFieldWidget sizeX, sizeY, sizeZ;

	public EditorPanelSelectedGizmo(EditorWidget parent) {
		super(parent, VoxEdit.id("selected_gizmo"), Text.translatable("voxedit.screen.panel.selectedGizmo"));
		
		EditorState.CHANGE_SELECTED.register((selected) -> rebuild());
		EditorState.MODIFY_GIZMO.register((gizmo) -> {
			if(gizmo == EditorState.selected()) reload();
		});
	}

	private void rebuild() {
		if(rebuilding) return;
		
		rebuilding = true;
		clearContent();
		
		Gizmo selected = EditorState.selected();
		if(selected instanceof Positionable p) {
			addContent(new LineHorizontal(getWidth(), Text.translatable("voxedit.gizmo.position")));
			x = new IntFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, (getWidth()-gap*2)/3, 20, Text.of("X"), 0, v -> {
				BlockPos pos = p.getPos();
				p.setPos(new BlockPos(v, pos.getY(), pos.getZ()));
			});
			y = new IntFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, (getWidth()-gap*2)/3, 20, Text.of("Y"), 0, v -> {
				BlockPos pos = p.getPos();
				p.setPos(new BlockPos(pos.getX(), v, pos.getZ()));
			});
			z = new IntFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, (getWidth()-gap*2)/3, 20, Text.of("Z"), 0, v -> {
				BlockPos pos = p.getPos();
				p.setPos(new BlockPos(pos.getX(), pos.getY(), v));
			});
			addContent(x);
			addContent(y);
			addContent(z);
		}
		if(selected instanceof Sizeable s) {
			addContent(new LineHorizontal(getWidth(), Text.translatable("voxedit.gizmo.size")));
			sizeX = new IntFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, (getWidth()-gap*2)/3, 20, Text.of("X"), 0, v -> {
				BlockPos size = s.getSize();
				s.setSize(new BlockPos(v, size.getY(), size.getZ()));
			});
			sizeY = new IntFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, (getWidth()-gap*2)/3, 20, Text.of("Y"), 0, v -> {
				BlockPos size = s.getSize();
				s.setSize(new BlockPos(size.getX(), v, size.getZ()));
			});
			sizeZ = new IntFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, (getWidth()-gap*2)/3, 20, Text.of("Z"), 0, v -> {
				BlockPos size = s.getSize();
				s.setSize(new BlockPos(size.getX(), size.getY(), v));
			});
			addContent(sizeX);
			addContent(sizeY);
			addContent(sizeZ);
		}
		if(selected instanceof Rotatable90Deg r) {
			addContent(new LineHorizontal(getWidth(), Text.translatable("voxedit.gizmo.rotation")));
			addContent(ButtonWidget.builder(Text.translatable("voxedit.gizmo.rotation.left"), (b) -> {
				r.setRotation(r.getRotation().rotate(BlockRotation.COUNTERCLOCKWISE_90));
			}).size((getWidth()-gap)/2, 20).build());
			addContent(ButtonWidget.builder(Text.translatable("voxedit.gizmo.rotation.right"), (b) -> {
				r.setRotation(r.getRotation().rotate(BlockRotation.CLOCKWISE_90));
			}).size((getWidth()-gap)/2, 20).build());
		}
		
		if(selected != null) {
			Text name = selected.getName();
			if(name != null) {
				addContent(new LineHorizontal(getWidth(), name));
				selected.addActions(new GizmoActions() {
					@Override
					public void add(Text text, Runnable action) {
						addContent(ButtonWidget.builder(text, (b) -> action.run()).size(getWidth(), 20).build());
					}

					@Override
					public void add(ToolSetting<?> setting, Supplier<ToolConfig> configGetter, Consumer<ToolConfig> configSetter, Consumer<ToolSetting<?>> notifier) {
						var settingWidget = ToolSettingWidget.of(setting, configGetter, configSetter, notifier);
						for(ClickableWidget widget : settingWidget.create(parent.getScreen(), 0, 0, width, 20)) {
							addContent(widget);
						}
					}
				});
			}
		}
		
		parent.refreshPositions();
		rebuilding = false;
		reload();
	}
	
	private void reload() {
		if(rebuilding) return;
		if(reloading) return;
		
		reloading = true;

		Gizmo selected = EditorState.selected();
		if(selected instanceof Positionable p) {
			x.setInt(p.getPos().getX());
			y.setInt(p.getPos().getY());
			z.setInt(p.getPos().getZ());
		}
		if(selected instanceof Sizeable s) {
			sizeX.setInt(s.getSize().getX());
			sizeY.setInt(s.getSize().getY());
			sizeZ.setInt(s.getSize().getZ());
		}
		
		reloading = false;
	}
}
