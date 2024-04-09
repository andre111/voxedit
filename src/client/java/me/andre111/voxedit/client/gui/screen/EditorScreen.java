package me.andre111.voxedit.client.gui.screen;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.GlStateManager.Viewport;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.client.ClientStates;
import me.andre111.voxedit.client.EditorState;
import me.andre111.voxedit.client.VoxEditClient;
import me.andre111.voxedit.client.gui.widget.EditorPanel;
import me.andre111.voxedit.client.gui.widget.EditorPanelPalette;
import me.andre111.voxedit.client.gui.widget.EditorPanelToolConfig;
import me.andre111.voxedit.client.gui.widget.EditorToolWidget;
import me.andre111.voxedit.client.gui.widget.EditorWidget;
import me.andre111.voxedit.client.renderer.SchematicRenderer;
import me.andre111.voxedit.client.renderer.SchematicView;
import me.andre111.voxedit.network.CPAction;
import me.andre111.voxedit.network.CPCommand;
import me.andre111.voxedit.network.Command;
import me.andre111.voxedit.tool.ConfiguredTool;
import me.andre111.voxedit.tool.Tool;
import me.andre111.voxedit.tool.data.Context;
import me.andre111.voxedit.tool.data.Target;
import me.andre111.voxedit.tool.data.ToolConfig;
import me.andre111.voxedit.tool.data.ToolSettings;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class EditorScreen extends Screen {
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

	private EditorScreen() {
		super(Text.translatable("voxedit.screen.main"));
		
		EditorState.CHANGE_SCHEMATIC.register((id, schematic) -> {
			Tool tool = EditorState.tool();
			if(tool == null) return;
			if(!id.equals(VoxEdit.id("preview."+tool.id().toTranslationKey()))) return;
			if(schematic == null) return;
			if(lastTarget == null) return;
			
			if(VoxEditClient.previewRenderer != null) {
				VoxEditClient.previewRenderer.close();
				VoxEditClient.previewRenderer = null;
			}
			
			BlockPos pos = lastTarget.pos().add(schematic.getOffsetX(), schematic.getOffsetY(), schematic.getOffsetZ());
			SchematicView view = new SchematicView(MinecraftClient.getInstance().world, pos, schematic);
			VoxEditClient.previewRenderer = new SchematicRenderer(view);
		});
	}

	public void rebuild() {
		
	}

	@Override
	public void init() {
		MinecraftClient.getInstance().getWindow().setScaleFactor(1);
		MinecraftClient.getInstance().options.hudHidden = true;
		isActive = true;
		
		if(widget == null) {
			widget = new EditorWidget(this);
			widget.setDimensions(width, height);
			addDrawableChild(widget);
	
			var toolPanel = widget.addPanel(EditorPanel.Location.LEFT, Text.translatable("voxedit.screen.panel.tools"));
			widget.addPanel(parent -> new EditorPanelToolConfig(parent, EditorPanel.Location.LEFT));
			widget.addPanel(parent -> new EditorPanelPalette(parent, EditorPanel.Location.LEFT));
	
			VoxEdit.TOOL_REGISTRY.forEach(tool -> {
				toolPanel.addChild(new EditorToolWidget(tool));
			});
		} else {
			addDrawableChild(widget);
		}

		widget.refreshPositions();
	}

	@Override
	public void tick() {
		if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_ALT)) {
			VoxEditClient.restoreGuiScale();
			MinecraftClient.getInstance().mouse.lockCursor();
			return;
		}
		MinecraftClient.getInstance().getWindow().setScaleFactor(1);
		
		if(lastTarget != null && lastTargetTicks++ == VoxEdit.PREVIEW_DELAY) {
			ConfiguredTool tool = EditorState.configuredTool();
			Context context = EditorState.context();
			if(tool != null && tool.tool().properties().showPreview() && context != null) {
				CPAction action = new CPAction(tool, List.of(lastTarget), context, CPAction.Action.PREVIEW);
				ClientPlayNetworking.send(action);
			}
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if(keyCode == GLFW.GLFW_KEY_ESCAPE) {
			if(dragging != -1) {
				dragging = -1;
				EditorState.clearTargets();
				updatePositions();
			} else {
				isActive = false;
				setLastTarget(null);
				close();
			}
			return true;
		}
		if(Screen.hasControlDown()) {
			if(VoxEditClient.UNDO.matchesKey(keyCode, scanCode)) {
				ClientPlayNetworking.send(new CPCommand(Command.UNDO));
				return true;
			}
			if(VoxEditClient.REDO.matchesKey(keyCode, scanCode)) {
				ClientPlayNetworking.send(new CPCommand(Command.REDO));
				return true;
			}
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
			if(dragging == -1) updateTarget(mouseX, mouseY, false);
			return;
		}
		
        super.mouseMoved(mouseX, mouseY);
    }
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if(!widget.isOverGui(mouseX, mouseY)) {
			var tool = EditorState.configuredTool();
			if(tool == null) return false;
			
			updateTarget(mouseX, mouseY, false);

			dragging = button;
			if(!tool.tool().properties().draggable()) {
				performActions();
				dragging = -1;
			}
			
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if(!widget.isOverGui(mouseX, mouseY)) {
			updateTarget(mouseX, mouseY, dragging != -1);
			return true;
		}
		
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if(dragging != -1) {
			updateTarget(mouseX, mouseY, true);
			performActions();
			dragging = -1;
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
		ConfiguredTool tool = EditorState.configuredTool();
		if(tool == null) return;
		
		// build ray from mouse coords
		float mx = (float) mouseX;
		float my = MinecraftClient.getInstance().getWindow().getHeight() - (float) mouseY;
		Vector3f origin = new Vector3f();
		Vector3f dir = new Vector3f();
		int[] viewport = new int[] { Viewport.getX(), Viewport.getY(), Viewport.getWidth(), Viewport.getHeight() };
		VoxEditClient.projectionMat.unprojectRay(mx, my, viewport, origin, dir);
		VoxEditClient.modelViewMat.invert().transformProject(dir);
	
		Vec3d start = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
		Vec3d end = start.add(new Vec3d(dir.x, dir.y, dir.z).normalize().multiply(128));
		
		// cast ray determine target
		boolean targetFluids = false;
		if(tool.tool().has(ToolSettings.TARGET_FLUIDS)) targetFluids = ToolSettings.TARGET_FLUIDS.get(tool.config());
		RaycastContext.FluidHandling fluidHandling = targetFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE;
		
		RaycastContext context = new RaycastContext(start, end, RaycastContext.ShapeType.VISUAL, fluidHandling, MinecraftClient.getInstance().cameraEntity);
		HitResult result = MinecraftClient.getInstance().world.raycast(context);
		if(result instanceof BlockHitResult blockHit) {
			Target target = new Target(blockHit.getBlockPos(), blockHit.getSide());
			if(target.equals(lastTarget)) return;
			
			setLastTarget(target);
			
			if(!EditorState.targets().contains(target)) {
				EditorState.target(target, add);
				updatePositions();
			}
		}
	}
	
	private void updatePositions() {
		ConfiguredTool tool = EditorState.configuredTool();
		if(tool == null) return;
		Context context = EditorState.context();
		if(context == null) return;
		
		Set<BlockPos> positions = new HashSet<BlockPos>();
		for(Target target : EditorState.targets()) positions.addAll(tool.tool().getBlockPositions(MinecraftClient.getInstance().world, target, context, tool.config()));
		ClientStates.instance().setPositions(positions);
	}
	
	private void performActions() {
		ConfiguredTool tool = EditorState.configuredTool();
		if(tool == null) return;
		Context context = EditorState.context();
		if(context == null) return;
		List<Target> targets = List.copyOf(EditorState.targets());
		if(targets.isEmpty()) return;
		
		if(EditorState.schematic(VoxEdit.id("preview."+tool.tool().id().toTranslationKey())) != null && dragging == 1) {
			CPAction action = new CPAction(tool, targets, context, CPAction.Action.APPLY_PREVIEW);
			ClientPlayNetworking.send(action);
		} else if(dragging == 1) {
			CPAction action = new CPAction(tool, targets, context, CPAction.Action.PLACE);
			ClientPlayNetworking.send(action);
		} else if(dragging == 0) {
			CPAction action = new CPAction(tool, targets, context, CPAction.Action.REMOVE);
			ClientPlayNetworking.send(action);
		}

		setLastTarget(null);
		dragging = -1;
	}
	
	private boolean updateRadius(int change) {
		if(EditorState.toolConfig().values().containsKey("radius")) {
			int radius = Integer.parseInt(EditorState.toolConfig().values().get("radius"));
			ToolConfig newConfig = EditorState.toolConfig().with("radius", Integer.toString(radius+change));
			if(EditorState.tool().isValid(newConfig)) {
				EditorState.toolConfig(newConfig);
				updatePositions();
				return true;
			}
		}
		return false;
	}
	
	private void setLastTarget(Target target) {
		if(Objects.equals(target, lastTarget)) return;
		
		if(EditorState.tool() != null) {
			EditorState.schematic(VoxEdit.id("preview."+EditorState.tool().id().toTranslationKey()), null);
		}
		if(VoxEditClient.previewRenderer != null) {
			VoxEditClient.previewRenderer.close();
			VoxEditClient.previewRenderer = null;
		}
		lastTarget = target;
		lastTargetTicks = 0;
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
	}

	@Override
	public void onDisplayed() {
		MinecraftClient.getInstance().getWindow().setScaleFactor(1);
		isActive = true;
	}

	@Override
	public void removed() {
		ClientStates.instance().setPositions(Collections.emptySet());
		MinecraftClient.getInstance().options.hudHidden = false;
		VoxEditClient.restoreGuiScale();
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	public boolean isActive() {
		return isActive;
	}
}
