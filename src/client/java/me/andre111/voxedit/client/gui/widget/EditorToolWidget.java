package me.andre111.voxedit.client.gui.widget;

import me.andre111.voxedit.client.EditorState;
import me.andre111.voxedit.tool.Tool;
import net.minecraft.client.gui.widget.ButtonWidget;

public class EditorToolWidget extends ButtonWidget {
	public EditorToolWidget(Tool tool) {
		super(0, 0, 48, 48, tool.asText(), (button) -> {
			EditorState.tool(tool);
		}, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
		
		
		EditorState.CHANGE_TOOL.register((newTool) -> {
			setAlpha(newTool.equals(tool) ? 1f : 0.5f);
		});
	}
}
