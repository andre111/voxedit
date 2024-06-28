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

import java.util.List;

import me.andre111.voxedit.client.EditorState;
import me.andre111.voxedit.client.gizmo.ActiveSelection;
import me.andre111.voxedit.selection.SelectionShape;
import me.andre111.voxedit.tool.Properties;
import me.andre111.voxedit.tool.data.Context;
import me.andre111.voxedit.tool.data.RaycastTargets;
import me.andre111.voxedit.tool.data.Target;
import me.andre111.voxedit.tool.data.ToolConfig;
import me.andre111.voxedit.tool.data.ToolSetting;
import me.andre111.voxedit.tool.data.ToolSettings;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

public class SelectTool extends ClientTool {
	public SelectTool() {
		super(Properties.of(ToolSettings.TARGET_FLUIDS).noPresets());
	}

	@Override
	public RaycastTargets getRaycastTargets(ToolConfig config) {
		if(ToolSettings.TARGET_FLUIDS.get(config)) {
			return RaycastTargets.BLOCKS_AND_FLUIDS;
		} else {
			return RaycastTargets.BLOCKS_ONLY;
		}
	}

	@Override
	public void mouseTargetMoved(Target target, Context context, ToolConfig config) {
		if(target == null) return;
		
		List<BlockPos> positions = EditorState.toolState().positions();
		if(positions.isEmpty()) positions.add(target.getBlockPos());
		else positions.set(positions.size()-1, target.getBlockPos());
		updateState(positions, config);
	}

	@Override
	public void mouseTargetClicked(int button, Target target, Context context, ToolConfig config) {
		if(target == null) return;
		
		List<BlockPos> positions = EditorState.toolState().positions();
		if(positions.isEmpty()) positions.add(target.getBlockPos());
		else positions.set(positions.size()-1, target.getBlockPos());
		updateState(positions, config);
		
		if(positions.size() == 2) {
			positions.clear();
		} else {
			EditorState.selected(null);
		}
		positions.add(target.getBlockPos());
	}
	
	@Override
	public void changedSetting(ToolSetting<?> setting, ToolConfig config) {
		List<BlockPos> positions = EditorState.toolState().positions();
		updateState(positions, config);
	}

	@Override
	public boolean cancel() {
		List<BlockPos> positions = EditorState.toolState().positions();
		if(positions.size() <= 1) return false;
		positions.clear();
		if(EditorState.selected() instanceof ActiveSelection sel) sel.cancel();
		return true;
	}
	
	private void updateState(List<BlockPos> positions, ToolConfig config) {
		if(positions.size() == 2) {
			BlockPos p1 = positions.get(0);
			BlockPos p2 = positions.get(1);
			
			// force "cube"
			if(Screen.hasShiftDown()) {
				int xd = p2.getX() - p1.getX();
				int yd = p2.getY() - p1.getY();
				int zd = p2.getZ() - p1.getZ();
				int size = Math.abs(xd);
				if(Math.abs(yd) > size) size = Math.abs(yd);
				if(Math.abs(zd) > size) size = Math.abs(zd);
				xd = xd < 0 ? -size : size;
				yd = yd < 0 ? -size : size;
				zd = zd < 0 ? -size : size;
				p2 = new BlockPos(p1.getX() + xd, p1.getY() + yd, p1.getZ() + zd);
			}
			
			// center on firt point
			if(Screen.hasControlDown()) {
				int xd = p2.getX() - p1.getX();
				int yd = p2.getY() - p1.getY();
				int zd = p2.getZ() - p1.getZ();
				p1 = new BlockPos(p1.getX() - xd, p1.getY() - yd, p1.getZ() - zd);
			}
			
			if(EditorState.selected() instanceof ActiveSelection sel) {
				sel.setSelection(new SelectionShape(BlockBox.encompassPositions(List.of(p1, p2)).get(), sel.getSelection().getShape()));
			} else {
				EditorState.selected(new ActiveSelection(new SelectionShape(BlockBox.encompassPositions(List.of(p1, p2)).get(), ToolSettings.BASE_SHAPE.get(config).shape()), config));
			}
		}
	}
}
