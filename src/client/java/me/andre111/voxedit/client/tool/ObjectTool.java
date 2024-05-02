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
import me.andre111.voxedit.client.gizmo.Positionable;
import me.andre111.voxedit.client.gui.screen.EditorScreen;
import me.andre111.voxedit.tool.Properties;
import me.andre111.voxedit.tool.data.Context;
import me.andre111.voxedit.tool.data.RaycastTargets;
import me.andre111.voxedit.tool.data.Target;
import me.andre111.voxedit.tool.data.ToolConfig;
import me.andre111.voxedit.tool.data.ToolSetting;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class ObjectTool extends ClientTool {
	public static final ToolSetting<Mode> MODE = ToolSetting.ofEnum("mode", Mode.class, Mode::asText, true);

	public ObjectTool() {
		super(Properties.of(MODE).noPresets());
	}

	@Override
	public void mouseMoved(Target target, Context context, ToolConfig config) {
		switch(MODE.get(config)) {
		case SELECT:
			break;
		case MOVE:
			if(EditorState.selected() instanceof Positionable p && target.pos().isPresent()) {
				if(EditorState.toolState().positions().isEmpty()) EditorState.toolState().positions().add(p.getPos().toImmutable());
				
				p.setPos(target.getBlockPos().offset(target.getSide()));
			}
			break;
		}
	}

	@Override
	public void mouseClicked(int button, Target target, Context context, ToolConfig config) {
		switch(MODE.get(config)) {
		case SELECT:
			Gizmo nearest = null;
			double nearestDist = 0;
			for(Gizmo gizmo : EditorState.gizmos()) {
				var result = Box.raycast(gizmo, EditorScreen.get().getLastRayStart(), EditorScreen.get().getLastRayEnd(), BlockPos.ORIGIN);
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
			break;
		case MOVE:
			break;
		}
		EditorState.toolConfig(config.with(MODE, Mode.SELECT));
		EditorState.toolState().positions().clear();
	}

	@Override
	public boolean cancel() {
		ToolConfig config = EditorState.toolConfig();
		switch(MODE.get(config)) {
		case SELECT:
			break;
		case MOVE:
			if(EditorState.selected() instanceof Positionable p && EditorState.toolState().positions().size() > 0) {
				p.setPos(EditorState.toolState().positions().getFirst());
			}
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
		MOVE;
		
		public Text asText() {
			return Text.translatable("voxedit.object.mode."+name().toLowerCase());
		}
	}
}
