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
package me.andre111.voxedit.client.gizmo;

import java.util.Iterator;
import java.util.List;

import me.andre111.voxedit.client.EditorState;
import me.andre111.voxedit.client.renderer.SelectionRenderer;
import me.andre111.voxedit.data.Setting;
import me.andre111.voxedit.data.Config;
import me.andre111.voxedit.data.CommonToolSettings;
import me.andre111.voxedit.selection.Selection;
import me.andre111.voxedit.selection.SelectionMode;
import me.andre111.voxedit.selection.SelectionShape;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ActiveSelection extends Gizmo implements Positionable, Renderable, Sizeable {
	public static final Setting<SelectionMode> SELECTION_MODE = Setting.ofEnum("selection_mode", SelectionMode.class, SelectionMode::asText, true);
	private static final SelectionRenderer RENDERER = new SelectionRenderer();
	
	private SelectionShape selection;
	private Config config;
	private boolean canceled = false;
	
	public ActiveSelection(SelectionShape selection, Config config) {
		this.selection = selection;
		this.config = config;
		RENDERER.rebuild(selection);
	}
	
	@Override
	public Iterator<Box> iterator() {
		return List.of(Box.from(selection.getBoundingBox())).iterator();
	}

	@Override
	public void addActions(GizmoActions actions) {
		actions.add(SELECTION_MODE, () -> config, (c) -> config = c, (s) -> {});
		actions.add(Text.translatable("voxedit.selection.apply"), () -> apply());
		actions.add(Text.translatable("voxedit.selection.cancel"), () -> cancel());
		actions.add(CommonToolSettings.BASE_SHAPE, () -> config, (c) -> config = c, (s) -> {
			selection = new SelectionShape(selection.getBoundingBox(), CommonToolSettings.BASE_SHAPE.get(config).value());
			RENDERER.rebuild(selection);
			modified();
		});
	}
	
	@Override
	public void addHandles(List<GizmoHandle> handles) {
		addPosHandles(handles);
	}

	@Override
	public Vec3d getHandleOrigin() {
		BlockPos size = getSize();
		return getPos().add(size.getX() / 2.0, size.getY() / 2.0, size.getZ() / 2.0);
	}

	@Override
	public void render(WorldRenderContext context) {
		RENDERER.draw(0.5f, 0.5f, 1f, context.camera().getPos(), context.frustum(), context.positionMatrix(), context.projectionMatrix(), MinecraftClient.getInstance().getWindow());
	}
	
	public SelectionShape getSelection() {
		return selection;
	}

	@Override
	public Text getName() {
		return Text.translatable("voxedit.selection");
	}

	@Override
	public Vec3d getPos() {
		BlockBox bb = selection.getBoundingBox();
		return new Vec3d(bb.getMinX(), bb.getMinY(), bb.getMinZ());
	}

	@Override
	public void setPos(Vec3d pos) {
		BlockPos bpos = new BlockPos((int) Math.round(pos.x), (int) Math.round(pos.y), (int) Math.round(pos.z));
		BlockPos max = bpos.add(getSize()).add(-1, -1, -1);
		selection = new SelectionShape(BlockBox.create(bpos, max), selection.getShape());
		RENDERER.rebuild(selection);
		modified();
	}

	@Override
	public BlockPos getSize() {
		BlockBox bb = selection.getBoundingBox();
		return new BlockPos(bb.getBlockCountX(), bb.getBlockCountY(), bb.getBlockCountZ());
	}

	@Override
	public void setSize(BlockPos size) {
		if(size.getX() <= 0) return;
		if(size.getY() <= 0) return;
		if(size.getZ() <= 0) return;
		BlockPos bpos = new BlockPos((int) Math.round(getPos().x), (int) Math.round(getPos().y), (int) Math.round(getPos().z));
		BlockPos max = bpos.add(size).add(-1, -1, -1);
		selection = new SelectionShape(BlockBox.create(bpos, max), selection.getShape());
		RENDERER.rebuild(selection);
		modified();
	}
	
	public void setSelection(SelectionShape selection) {
		this.selection = selection;
		RENDERER.rebuild(selection);
		modified();
	}
	
	public void apply() {
		EditorState.removeGizmo(this);
	}
	
	public void cancel() {
		canceled = true;
		EditorState.removeGizmo(this);
	}

	@Override
	public void deselected() {
		if(canceled) return;
		
		EditorState.persistant().selection(Selection.combine(EditorState.persistant().selection(), selection, SELECTION_MODE.get(config)));
		EditorState.removeGizmo(this);
	}
}
