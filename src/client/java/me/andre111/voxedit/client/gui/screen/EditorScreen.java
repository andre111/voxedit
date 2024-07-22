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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.GlStateManager.Viewport;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.VoxEditUtil;
import me.andre111.voxedit.client.EditorLayout;
import me.andre111.voxedit.client.EditorState;
import me.andre111.voxedit.client.VoxEditClient;
import me.andre111.voxedit.client.gizmo.ActiveSelection;
import me.andre111.voxedit.client.gizmo.Gizmo;
import me.andre111.voxedit.client.gizmo.GizmoHandle;
import me.andre111.voxedit.client.gizmo.Renderable;
import me.andre111.voxedit.client.gui.widget.EditorPanelFilter;
import me.andre111.voxedit.client.gui.widget.EditorPanelHistory;
import me.andre111.voxedit.client.gui.widget.EditorPanelPalette;
import me.andre111.voxedit.client.gui.widget.EditorPanelSchematics;
import me.andre111.voxedit.client.gui.widget.EditorPanelSelectedGizmo;
import me.andre111.voxedit.client.gui.widget.EditorPanelToolConfig;
import me.andre111.voxedit.client.gui.widget.EditorPanelTools;
import me.andre111.voxedit.client.gui.widget.EditorWidget;
import me.andre111.voxedit.client.gui.widget.MenuBarWidget;
import me.andre111.voxedit.client.network.ClientNetworking;
import me.andre111.voxedit.client.renderer.SchematicRenderer;
import me.andre111.voxedit.client.renderer.SchematicView;
import me.andre111.voxedit.client.renderer.SelectionRenderer;
import me.andre111.voxedit.client.tool.ClientTool;
import me.andre111.voxedit.client.tool.ObjectTool;
import me.andre111.voxedit.data.Context;
import me.andre111.voxedit.data.RaycastTargets;
import me.andre111.voxedit.data.Target;
import me.andre111.voxedit.data.Config;
import me.andre111.voxedit.data.ConfigValue;
import me.andre111.voxedit.data.CommonToolSettings;
import me.andre111.voxedit.network.CPAction;
import me.andre111.voxedit.network.CPCommand;
import me.andre111.voxedit.network.CPSelection;
import me.andre111.voxedit.network.Command;
import me.andre111.voxedit.selection.SelectionSet;
import me.andre111.voxedit.shape.Shape;
import me.andre111.voxedit.tool.Tool;
import me.andre111.voxedit.tool.Tool.Action;
import me.andre111.voxedit.tool.VoxelTool;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;

public class EditorScreen extends Screen implements UnscaledScreen {
	private static EditorScreen INSTANCE;

	public static EditorScreen get() {
		if(INSTANCE == null) INSTANCE = new EditorScreen();
		return INSTANCE;
	}

	private EditorWidget widget;
	private boolean isActive = false;
	private int dragging = -1;
	private Target lastTarget = null;
	private int lastTargetTicks = 0;
	private Vec3d lastRayStart = null;
	private Vec3d lastRayEnd = null;

	private Matrix4f modelViewMat = new Matrix4f();
	private Matrix4f projectionMat = new Matrix4f();
	private SelectionRenderer positionsRenderer = new SelectionRenderer();
	private SelectionRenderer selectionRenderer = new SelectionRenderer();
	private SchematicRenderer previewRenderer;

	private EditorScreen() {
		super(Text.translatable("voxedit.screen.main"));

		EditorState.CHANGE_SCHEMATIC.register((id, schematic) -> {
			Tool tool = EditorState.tool();
			if(tool == null) return;
			if(!id.equals("voxedit.preview."+tool.id().toTranslationKey())) return;
			if(schematic == null) return;
			if(lastTarget == null) return;
			if(lastTarget.pos().isEmpty()) return;

			if(previewRenderer != null) {
				previewRenderer.close();
				previewRenderer = null;
			}

			BlockPos pos = lastTarget.getBlockPos().add(schematic.getOffsetX(), schematic.getOffsetY(), schematic.getOffsetZ());
			SchematicView view = new SchematicView(pos, schematic);
			previewRenderer = new SchematicRenderer(view);
		});
		EditorState.CHANGE_SELECTION.register(() -> {
			selectionRenderer.rebuild(EditorState.persistant().selection());
		});
		WorldRenderEvents.LAST.register(this::renderInWorld);
	}

	@Override
	public void init() {
		VoxEditClient.unscaleGui();
		MinecraftClient.getInstance().options.hudHidden = true;
		isActive = true;
		ClientNetworking.sendCommand(Command.EDITOR_ACTIVATE);
		width = MinecraftClient.getInstance().getWindow().getFramebufferWidth();
		height = MinecraftClient.getInstance().getWindow().getFramebufferHeight();

		if(widget == null) {
			widget = new EditorWidget(this);
			widget.setDimensions(width, height);
			addDrawableChild(widget);

			MenuBarWidget menu = widget.getMenu();
			menu.addCategory(Text.translatable("voxedit.screen.menu.edit"))
			.addEntry(Text.translatable("voxedit.screen.menu.edit.undo"), () -> { ClientNetworking.sendCommand(Command.UNDO); })
			.addEntry(Text.translatable("voxedit.screen.menu.edit.redo"), () -> { ClientNetworking.sendCommand(Command.REDO); });
			menu.addCategory(Text.translatable("voxedit.screen.menu.selection"))
			.addEntry(Text.translatable("voxedit.screen.menu.selection.clear"), () -> { 
				if(EditorState.selected() instanceof ActiveSelection sel) sel.cancel(); 
				EditorState.persistant().selection(null);
			})
			.addEntry(Text.translatable("voxedit.screen.menu.selection.save"), () -> {
				if(EditorState.selected() instanceof ActiveSelection sel) sel.apply();
				if(EditorState.persistant().selection() != null) {
					InputScreen.getString(this, Text.translatable("voxedit.prompt.schematic.name"), "", name -> {
						if(name == null || name.isBlank()) return;
						ClientPlayNetworking.send(new CPSelection(EditorState.persistant().selection()));
						ClientNetworking.sendCommand(Command.SAVE_SCHEMATIC, name);
					});
				}
			});
			menu.addCategory(Text.translatable("voxedit.screen.menu.settings"))
			.addEntry(Text.translatable("voxedit.screen.menu.settings.keys"), () -> {});

			widget.addPanel(parent -> new EditorPanelTools(parent), EditorWidget.Location.LEFT);
			widget.addPanel(parent -> new EditorPanelToolConfig(parent), EditorWidget.Location.LEFT);
			widget.addPanel(parent -> new EditorPanelPalette(parent), EditorWidget.Location.LEFT);
			widget.addPanel(parent -> new EditorPanelFilter(parent), EditorWidget.Location.LEFT);

			widget.addPanel(parent -> new EditorPanelHistory(parent), EditorWidget.Location.RIGHT);
			widget.addPanel(parent -> new EditorPanelSchematics(parent), EditorWidget.Location.RIGHT);
			widget.addPanel(parent -> new EditorPanelSelectedGizmo(parent), EditorWidget.Location.RIGHT);

			widget.loadLayout(VoxEditUtil.readJson(VoxEditClient.dataPath().resolve("editor_layout.json"), EditorLayout.CODEC, EditorLayout.EMPTY));
		} else {
			widget.setDimensions(width, height);
			addDrawableChild(widget);
		}

		widget.refreshPositions();
		
		//TODO: remove this hack
		EditorState.CHANGE_FILTER.invoker().accept(EditorState.filter());
	}

	@Override
	public void tick() {
		if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_ALT)) {
			VoxEditClient.restoreGuiScale();
			MinecraftClient.getInstance().mouse.lockCursor();
			return;
		}
		VoxEditClient.unscaleGui();

		if(lastTarget != null && lastTargetTicks++ == VoxEdit.PREVIEW_DELAY) {
			var tool = EditorState.configuredTool();
			Context context = EditorState.context();
			if(tool != null && tool.value().properties().showPreview() && context != null) {
				CPAction action = new CPAction(tool, List.of(lastTarget), context, Action.PREVIEW);
				ClientPlayNetworking.send(action);
			}
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if(keyCode == GLFW.GLFW_KEY_ESCAPE) {
			if(EditorState.tool() instanceof ClientTool clientTool && clientTool.cancel()) {
				// handled in tool
			} else if(dragging != -1) {
				dragging = -1;
				EditorState.clearTargets();
				updatePositions();
			} else {
				isActive = false;
				ClientNetworking.sendCommand(Command.EDITOR_DEACTIVATE);
				setLastTarget(null);
				close();
			}
			return true;
		}
		if(Screen.hasControlDown()) {
			if(VoxEditClient.UNDO.matchesKey(keyCode, scanCode)) {
				ClientPlayNetworking.send(new CPCommand(Command.UNDO, ""));
				return true;
			}
			if(VoxEditClient.REDO.matchesKey(keyCode, scanCode)) {
				ClientPlayNetworking.send(new CPCommand(Command.REDO, ""));
				return true;
			}
		}
		if(VoxEditClient.INCREASE_SPEED.wasPressed()) {
			EditorState.cameraSpeed(Math.min(EditorState.cameraSpeed()+1, 10f));
			statusMessage(Text.translatable("voxedit.feedback.cameraSpeed", EditorState.cameraSpeed()));
			return true;
		}
		if(VoxEditClient.DECREASE_SPEED.wasPressed()) {
			EditorState.cameraSpeed(Math.max(1f, EditorState.cameraSpeed()-1));
			statusMessage(Text.translatable("voxedit.feedback.cameraSpeed", EditorState.cameraSpeed()));
			return true;
		}
		if(VoxEditClient.INCREASE_RADIUS.matchesKey(keyCode, scanCode)) {
			if(updateRadius(1)) return true;
		}
		if(VoxEditClient.DECREASE_RADIUS.matchesKey(keyCode, scanCode)) {
			if(updateRadius(-1)) return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		if(!widget.isOverGui(mouseX, mouseY)) {
			//if(dragging == -1) updateTarget(mouseX, mouseY, false);
			updateTarget(mouseX, mouseY, dragging != -1);

			// client tool
			var tool = EditorState.configuredTool();
			if(tool != null && tool.value() instanceof ClientTool clientTool) {
				clientTool.mouseMoved(dragging, EditorState.context(), tool.config());
			}
			return;
		}

		super.mouseMoved(mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if(!widget.isOverGui(mouseX, mouseY)) {
			var tool = EditorState.configuredTool();
			if(tool == null) return false;

			//TODO: NOTE: for some reason the mouse coordinates in clicked, dragged and released are unreliable?
			//updateTarget(mouseX, mouseY, false);

			dragging = button;
			if(!tool.value().properties().draggable()) {
				performActions();
				dragging = -1;
			}


			// client tool
			if(tool != null && tool.value() instanceof ClientTool clientTool) {
				clientTool.mousePressed(button, EditorState.context(), tool.config());
			}
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if(!widget.isOverGui(mouseX, mouseY)) {
			//TODO: NOTE: for some reason the mouse coordinates in clicked, dragged and released are unreliable?
			//updateTarget(mouseX, mouseY, dragging != -1);
			return true;
		}

		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if(dragging != -1) {
			//TODO: NOTE: for some reason the mouse coordinates in clicked, dragged and released are unreliable?
			//updateTarget(mouseX, mouseY, true);
			performActions();
			dragging = -1;

			// client tool
			var tool = EditorState.configuredTool();
			if(tool != null && tool.value() instanceof ClientTool clientTool) {
				clientTool.mouseReleased(button, EditorState.context(), tool.config());
			}
			return true;
		}

		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if(!widget.isOverGui(mouseX, mouseY)) {
			var tool = EditorState.configuredTool();
			if(tool == null) return false;

			updateRadius((int) verticalAmount);

			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private void updateTarget(double mouseX, double mouseY, boolean add) {
		var tool = EditorState.configuredTool();
		if(tool == null) return;
		RaycastTargets raycastTargets = tool.value().getRaycastTargets(tool.config());
		if(!raycastTargets.targetBlocks() && !raycastTargets.targetEntities()) return;

		// build ray from mouse coords
		double maxDist = 256;
		float mx = (float) mouseX;
		float my = MinecraftClient.getInstance().getWindow().getHeight() - (float) mouseY;
		Vector3f origin = new Vector3f();
		Vector3f dir = new Vector3f();
		int[] viewport = new int[] { Viewport.getX(), Viewport.getY(), Viewport.getWidth(), Viewport.getHeight() };
		projectionMat.unprojectRay(mx, my, viewport, origin, dir);
		modelViewMat.invert().transformProject(dir);

		Vec3d start = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
		Vec3d end = start.add(new Vec3d(dir.x, dir.y, dir.z).normalize().multiply(maxDist));

		lastRayStart = start;
		lastRayEnd = end;

		// prepare
		double nearestDist = maxDist;
		UUID nearestEntity = null;
		BlockPos blockPos = null;
		Direction blockSide = null;

		// find hit entity
		if(raycastTargets.targetEntities()) {
			Box box = new Box(start.x, start.y, start.z, end.x, end.y, end.z);
			List<Entity> entities = MinecraftClient.getInstance().world.getOtherEntities(MinecraftClient.getInstance().player, box, entity -> !entity.isPlayer());
			for(Entity entity : entities) {
				Box entityBox = entity.getBoundingBox().expand(entity.getTargetingMargin());
				Optional<Vec3d> hitPos = entityBox.raycast(start, end);
				if(hitPos.isEmpty()) continue;
				double dist = hitPos.get().distanceTo(start);
				if(dist >= nearestDist) continue;
				nearestDist = dist;
				nearestEntity = entity.getUuid();
			}
		}

		// find hit block
		if(raycastTargets.targetBlocks()) {
			RaycastContext.FluidHandling fluidHandling = raycastTargets.targetFluids() ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE;
			RaycastContext context = new RaycastContext(start, end, RaycastContext.ShapeType.VISUAL, fluidHandling, MinecraftClient.getInstance().cameraEntity);
			HitResult result = MinecraftClient.getInstance().world.raycast(context);
			if(result instanceof BlockHitResult blockHit && blockHit.getType() != HitResult.Type.MISS) {
				blockPos = blockHit.getBlockPos();
				blockSide = blockHit.getSide();
			}
		}

		// set target
		if(!raycastTargets.targetOther() && blockPos == null && nearestEntity == null) return;

		Target target = new Target(Optional.ofNullable(blockPos), Optional.ofNullable(blockSide), Optional.ofNullable(nearestEntity));
		if(target.equals(lastTarget)) return;

		setLastTarget(target);

		if(!EditorState.targets().contains(target)) {
			EditorState.target(target, add);
			updatePositions();
		}
	}

	private void updatePositions() {
		var tool = EditorState.configuredTool();
		if(tool == null) return;
		Context context = EditorState.context();
		if(context == null) return;

		Set<BlockPos> positions = new HashSet<BlockPos>();
		if(tool.value() instanceof VoxelTool voxelTool) {
			for(Target target : EditorState.targets()) {
				positions.addAll(voxelTool.getBlockPositions(MinecraftClient.getInstance().world, target, context, tool.config()));
			}
		}
		EditorState.positions(positions);
		positionsRenderer.rebuild(new SelectionSet(positions));
	}

	private void performActions() {
		var tool = EditorState.configuredTool();
		if(tool == null) return;
		Context context = EditorState.context();
		if(context == null) return;
		List<Target> targets = List.copyOf(EditorState.targets());
		if(targets.isEmpty()) return;

		if(tool.value() instanceof ClientTool clientTool) {
			clientTool.mouseTargetClicked(dragging, targets.getLast(), context, tool.config());
		} else if(EditorState.schematic("voxedit.preview."+tool.value().id().toTranslationKey()) != null && dragging == 1) {
			CPAction action = new CPAction(tool, targets, context, Action.APPLY_PREVIEW);
			ClientPlayNetworking.send(action);
		} else if(dragging == 1) {
			CPAction action = new CPAction(tool, targets, context, Action.ADD_OR_MODIFY);
			ClientPlayNetworking.send(action);
		} else if(dragging == 0) {
			CPAction action = new CPAction(tool, targets, context, Action.REMOVE);
			ClientPlayNetworking.send(action);
		}

		setLastTarget(null);
		dragging = -1;
	}

	private boolean updateRadius(int change) {
		if(EditorState.tool() == null) return false;
		if(EditorState.toolConfig() == null) return false;

		Config newConfig = null;
		if(EditorState.tool().has(CommonToolSettings.SHAPE)) {
			var config = EditorState.toolConfig();
			var shape = CommonToolSettings.SHAPE.get(config);
			var size = Shape.SIZE.get(shape.config());
			if(!size.split()) {
				newConfig = config.modify(CommonToolSettings.SHAPE, c -> c.with(c.config().modify(Shape.SIZE, s -> s.size(s.x()+change))));
			}
		} else if(EditorState.toolConfig().values().get("radius") instanceof ConfigValue.CVString stringValue) {
			int radius = Integer.parseInt(stringValue.get());
			newConfig = EditorState.toolConfig().withRaw("radius", new ConfigValue.CVString(Integer.toString(radius+change)));
		}
		if(newConfig != null && EditorState.tool().isValid(newConfig)) {
			EditorState.toolConfig(newConfig);
			updatePositions();
			return true;
		}
		return false;
	}

	private void setLastTarget(Target target) {
		if(Objects.equals(target, lastTarget)) return;

		if(EditorState.tool() != null) {
			EditorState.schematic("voxedit.preview."+EditorState.tool().id().toTranslationKey(), null);
		}
		if(previewRenderer != null) {
			previewRenderer.close();
			previewRenderer = null;
		}
		lastTarget = target;
		lastTargetTicks = 0;

		if(EditorState.tool() instanceof ClientTool clientTool) {
			clientTool.mouseTargetMoved(target, EditorState.context(), EditorState.toolConfig());
		}
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
	}

	private void renderInWorld(WorldRenderContext context) {
		modelViewMat.set(context.positionMatrix());
		projectionMat.set(context.projectionMatrix());
		if(!isActive) return;

		if(previewRenderer != null) {
			previewRenderer.draw(Vec3i.ZERO, context.camera().getPos(), context.frustum(), context.positionMatrix(), context.projectionMatrix(), true);
		}
		for(Gizmo gizmo : EditorState.gizmos()) {
			if(gizmo instanceof Renderable renderable) {
				renderable.render(context);
			}
		}
		if(EditorState.selected() != null && EditorState.tool() instanceof ObjectTool) {
			for(GizmoHandle handle : EditorState.gizmoHandles()) {
				handle.render(context, EditorState.selected());
			}
		}
		if(!EditorState.targets().isEmpty()) {
			positionsRenderer.draw(1f, 1f, 1f, context.camera().getPos(), context.frustum(), context.positionMatrix(), context.projectionMatrix(), MinecraftClient.getInstance().getWindow());
		}
		if(EditorState.persistant().selection() != null) {
			selectionRenderer.draw(1f, 1f, 1f, context.camera().getPos(), context.frustum(), context.positionMatrix(), context.projectionMatrix(), MinecraftClient.getInstance().getWindow());
		}
	}

	@Override
	public void onDisplayed() {
		VoxEditClient.unscaleGui();
		isActive = true;
		ClientNetworking.sendCommand(Command.EDITOR_ACTIVATE);
	}

	@Override
	public void removed() {
		EditorState.positions(Collections.emptySet());
		MinecraftClient.getInstance().options.hudHidden = false;
		VoxEditClient.restoreGuiScale();
	}

	@Override
	public boolean shouldPause() {
		return false; // would not update world rendering otherwise
	}

	public void onLayoutChange() {
		VoxEditUtil.writeJson(VoxEditClient.dataPath().resolve("editor_layout.json"), EditorLayout.CODEC, widget.getLayout());
	}

	public boolean isActive() {
		return isActive;
	}

	public void statusMessage(Text text) {
		widget.getStatusBar().setStatus(text);
	}

	public Vec3d getLastRayStart() {
		return lastRayStart;
	}

	public Vec3d getLastRayEnd() {
		return lastRayEnd;
	}
}
