package me.andre111.voxedit.client.gui.widget;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.client.EditorState;
import me.andre111.voxedit.tool.Tool;
import net.minecraft.text.Text;

public class EditorPanelTools extends EditorPanel {
	private final SelectionWidget<Tool> selection;

	public EditorPanelTools(EditorWidget parent) {
		super(parent, VoxEdit.id("tools"), Text.translatable("voxedit.screen.panel.tools"));
		
		selection = new SelectionWidget<>(width, 48, 48, null, (tool) -> EditorState.tool(tool));
		selection.setPadding(2);
		selection.setGap(2);
		VoxEdit.TOOL_REGISTRY.forEach(tool -> selection.addOption(tool, tool.asText()));
		addContent(selection);
	}
	

	@Override
	public void refreshPositions() {
		selection.setWidth(width);
		super.refreshPositions();
	}
}
