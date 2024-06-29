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
package me.andre111.voxedit.client.tool;

import me.andre111.voxedit.client.EditorState;
import me.andre111.voxedit.client.gizmo.Gizmo;
import me.andre111.voxedit.client.gizmo.GizmoHandle;
import me.andre111.voxedit.client.gizmo.Positionable;
import me.andre111.voxedit.client.gui.screen.EditorScreen;
import me.andre111.voxedit.tool.Properties;
import me.andre111.voxedit.tool.data.Context;
import me.andre111.voxedit.tool.data.RaycastTargets;
import me.andre111.voxedit.tool.data.Target;
import me.andre111.voxedit.tool.data.ToolConfig;
import me.andre111.voxedit.tool.data.ToolSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ObjectTool extends ClientTool {
	public static final ToolSetting<Mode> MODE = ToolSetting.ofEnum("mode", Mode.class, Mode::asText, true);

	public ObjectTool() {
		super(Properties.of(MODE).draggable().noPresets());
	}

	@Override
	public void mouseMoved(int button, Context context, ToolConfig config) {
		if(button == -1 && MODE.get(config) == Mode.ADJUST) {
			EditorState.toolConfig(config.with(MODE, Mode.SELECT));
			EditorState.toolState().positions().clear();
			EditorState.activeHandle(null, null);
			return;
		}
		
		if(button == 0 && EditorState.activeHandle() != null) {
			Vec3d rayStart = EditorScreen.get().getLastRayStart();
			Vec3d rayEnd = EditorScreen.get().getLastRayEnd();

			// find position where ray intersects the three planes
			Vec3d handleOrigin = EditorState.activeHandleOrigin();
			Vec3d rayDir = new Vec3d(rayEnd.x, rayEnd.y, rayEnd.z).subtract(rayStart);

			Vec3d pos = null;
			switch(EditorState.activeHandle().mode()) {
			case XY_PLANE:
				if(rayDir.z != 0) {
					double dist = (handleOrigin.z - rayStart.z) / rayDir.z;
					if(dist > 0) pos = new Vec3d(rayDir.x, rayDir.y, rayDir.z).multiply(dist).add(rayStart).subtract(handleOrigin);
				}
				break;
			case XZ_PLANE:
				if(rayDir.y != 0) {
					double dist = (handleOrigin.y - rayStart.y) / rayDir.y;
					if(dist > 0) pos = new Vec3d(rayDir.x, rayDir.y, rayDir.z).multiply(dist).add(rayStart).subtract(handleOrigin);
				}
				break;
			case ZY_PLANE:
				if(rayDir.x != 0) {
					double dist = (handleOrigin.x - rayStart.x) / rayDir.x;
					if(dist > 0) pos = new Vec3d(rayDir.x, rayDir.y, rayDir.z).multiply(dist).add(rayStart).subtract(handleOrigin);
				}
				break;
			}

			if(pos != null) {
				EditorState.activeHandle().action().change((float) pos.x, (float) pos.y, (float) pos.z);
			}
		}
	}

	@Override
	public void mousePressed(int button, Context context, ToolConfig config) {
		if(button == 0 && MODE.get(config) == Mode.SELECT) {
			Vec3d rayStart = EditorScreen.get().getLastRayStart();
			Vec3d rayEnd = EditorScreen.get().getLastRayEnd();

			// "select" gizmo handles
			if(EditorState.selected() != null) {
				// find position where ray intersects the three planes
				Vec3d handleOrigin = EditorState.selected().getHandleOrigin();
				Vec3d rayDir = new Vec3d(rayEnd.x, rayEnd.y, rayEnd.z).subtract(rayStart);
				Vec3d gho = handleOrigin.subtract(MinecraftClient.getInstance().gameRenderer.getCamera().getPos());
				float scale = (float) MathHelper.clamp(Math.sqrt(gho.x*gho.x + gho.y*gho.y + gho.z*gho.z) / 10.0, 1, 16);

				Vec3d xyPlanePos = null;
				Vec3d xzPlanePos = null;
				Vec3d zyPlanePos = null;

				if(rayDir.x != 0) {
					double dist = (handleOrigin.x - rayStart.x) / rayDir.x;
					if(dist > 0) zyPlanePos = new Vec3d(rayDir.x, rayDir.y, rayDir.z).multiply(dist).add(rayStart).subtract(handleOrigin);
				}
				if(rayDir.y != 0) {
					double dist = (handleOrigin.y - rayStart.y) / rayDir.y;
					if(dist > 0) xzPlanePos = new Vec3d(rayDir.x, rayDir.y, rayDir.z).multiply(dist).add(rayStart).subtract(handleOrigin);
				}
				if(rayDir.z != 0) {
					double dist = (handleOrigin.z - rayStart.z) / rayDir.z;
					if(dist > 0) xyPlanePos = new Vec3d(rayDir.x, rayDir.y, rayDir.z).multiply(dist).add(rayStart).subtract(handleOrigin);
				}

				// find targeted gizmo handle
				GizmoHandle targeted = null;
				Vec3d targetPos = null;
				for(GizmoHandle handle : EditorState.gizmoHandles()) {
					switch(handle.mode()) {
					case XY_PLANE:
						if(xyPlanePos == null) continue;
						if(xyPlanePos.x < handle.start1()*scale || xyPlanePos.x > (handle.start1() + handle.size1())*scale) continue;
						if(xyPlanePos.y < handle.start2()*scale || xyPlanePos.y > (handle.start2() + handle.size2())*scale) continue;
						targeted = handle;
						targetPos = xyPlanePos;
						break;
					case XZ_PLANE:
						if(xzPlanePos == null) continue;
						if(xzPlanePos.x < handle.start1()*scale || xzPlanePos.x > (handle.start1() + handle.size1())*scale) continue;
						if(xzPlanePos.z < handle.start2()*scale || xzPlanePos.z > (handle.start2() + handle.size2())*scale) continue;
						targeted = handle;
						targetPos = xzPlanePos;
						break;
					case ZY_PLANE:
						if(zyPlanePos == null) continue;
						if(zyPlanePos.z < handle.start1()*scale || zyPlanePos.z > (handle.start1() + handle.size1())*scale) continue;
						if(zyPlanePos.y < handle.start2()*scale || zyPlanePos.y > (handle.start2() + handle.size2())*scale) continue;
						targeted = handle;
						targetPos = zyPlanePos;
						break;
					default:
					}
				}

				System.out.println(targeted+" "+xzPlanePos+" "+handleOrigin);
				if(targeted != null) {
					EditorState.activeHandle(targeted, handleOrigin);
					targeted.action().activate((float) targetPos.x, (float) targetPos.y, (float) targetPos.z);
					EditorState.toolConfig(config.with(MODE, Mode.ADJUST));
					EditorState.toolState().positions().clear();
				}
			}
		}
	}

	@Override
	public void mouseReleased(int button, Context context, ToolConfig config) {
		if(button == 0) {
			EditorState.toolConfig(config.with(MODE, Mode.SELECT));
			EditorState.toolState().positions().clear();
			EditorState.activeHandle(null, null);
		}
	}

	@Override
	public void mouseTargetMoved(Target target, Context context, ToolConfig config) {
		switch(MODE.get(config)) {
		case SELECT:
			break;
		case POSITION:
			if(EditorState.selected() instanceof Positionable p && target.pos().isPresent()) {
				if(EditorState.toolState().positions().isEmpty()) EditorState.toolState().positions().add(p.getPos().toImmutable());

				p.setPos(target.getBlockPos().offset(target.getSide()));
			}
			break;
		case ADJUST:
			break;
		}
	}

	@Override
	public void mouseTargetClicked(int button, Target target, Context context, ToolConfig config) {
		switch(MODE.get(config)) {
		case SELECT:
			Vec3d rayStart = EditorScreen.get().getLastRayStart();
			Vec3d rayEnd = EditorScreen.get().getLastRayEnd();

			// select new gizmo
			Gizmo nearest = null;
			double nearestDist = 0;
			for(Gizmo gizmo : EditorState.gizmos()) {
				var result = Box.raycast(gizmo, rayStart, rayEnd, BlockPos.ORIGIN);
				if(result != null) {
					double dist = result.getPos().squaredDistanceTo(EditorScreen.get().getLastRayStart());
					if(nearest == null || dist < nearestDist) {
						nearest = gizmo;
						nearestDist = dist;
					}
				}
			}
			if(nearest != null) {
				EditorState.selected(nearest);
			}
			EditorState.toolConfig(config.with(MODE, Mode.SELECT));
			EditorState.toolState().positions().clear();
			break;
		case POSITION:
			EditorState.toolConfig(config.with(MODE, Mode.SELECT));
			EditorState.toolState().positions().clear();
			break;
		case ADJUST:
			break;
		}
	}

	@Override
	public boolean cancel() {
		ToolConfig config = EditorState.toolConfig();
		switch(MODE.get(config)) {
		case SELECT:
			break;
		case POSITION:
			if(EditorState.selected() instanceof Positionable p && EditorState.toolState().positions().size() > 0) {
				p.setPos(EditorState.toolState().positions().getFirst());
			}
			EditorState.activeHandle(null, null);
			EditorState.toolConfig(config.with(MODE, Mode.SELECT));
			EditorState.toolState().positions().clear();
			return true;
		case ADJUST:
			if(EditorState.activeHandle() != null) EditorState.activeHandle().action().cancel();
			EditorState.activeHandle(null, null);
			EditorState.toolConfig(config.with(MODE, Mode.SELECT));
			EditorState.toolState().positions().clear();
			return true;
		}
		return false;
	}

	@Override
	public RaycastTargets getRaycastTargets(ToolConfig config) {
		return RaycastTargets.BLOCKS_AND_OTHER;
	}

	public static enum Mode {
		SELECT,
		POSITION,
		ADJUST;

		public Text asText() {
			return Text.translatable("voxedit.object.mode."+name().toLowerCase());
		}
	}
}
