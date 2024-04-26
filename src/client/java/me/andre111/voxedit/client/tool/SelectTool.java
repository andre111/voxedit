package me.andre111.voxedit.client.tool;

import java.util.List;

import me.andre111.voxedit.client.EditorState;
import me.andre111.voxedit.selection.Selection;
import me.andre111.voxedit.selection.SelectionMode;
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
	private static final ToolSetting<SelectionMode> SELECTION_MODE = ToolSetting.ofEnum("selection_mode", SelectionMode.class, SelectionMode::asText, true);
	
	public SelectTool() {
		super(Properties.of(ToolSettings.BASE_SHAPE, ToolSettings.TARGET_FLUIDS, SELECTION_MODE).noPresets());
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
	public void mouseMoved(Target target, Context context, ToolConfig config) {
		if(target == null) return;
		
		List<BlockPos> positions = EditorState.toolState().positions();
		if(positions.isEmpty()) positions.add(target.getBlockPos());
		else positions.set(positions.size()-1, target.getBlockPos());
		updateState(positions, false, config);
	}

	@Override
	public void mouseClicked(int button, Target target, Context context, ToolConfig config) {
		if(target == null) return;
		
		List<BlockPos> positions = EditorState.toolState().positions();
		if(positions.isEmpty()) positions.add(target.getBlockPos());
		else positions.set(positions.size()-1, target.getBlockPos());
		updateState(positions, true, config);
		positions.add(target.getBlockPos());
	}

	@Override
	public boolean cancel() {
		List<BlockPos> positions = EditorState.toolState().positions();
		if(positions.size() <= 1) return false;
		positions.clear();
		EditorState.persistant().activeSelection(null);
		return true;
	}
	
	private void updateState(List<BlockPos> positions, boolean add, ToolConfig config) {
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
			
			EditorState.persistant().activeSelection(new SelectionShape(BlockBox.encompassPositions(List.of(p1, p2)).get(), ToolSettings.BASE_SHAPE.get(config).shape()));
			if(add) {
				positions.clear();
				EditorState.persistant().selection(Selection.combine(EditorState.persistant().selection(), EditorState.persistant().activeSelection(), SELECTION_MODE.get(config)));
				EditorState.persistant().activeSelection(null);
			}
		}
	}
}
