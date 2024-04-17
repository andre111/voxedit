package me.andre111.voxedit.client.gui.widget;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.client.EditorState;
import net.minecraft.text.Text;

public class EditorPanelSchematics extends EditorPanel {

	public EditorPanelSchematics(EditorWidget parent) {
		super(parent, VoxEdit.id("schematics"), Text.translatable("voxedit.screen.panel.schematics"));
		
		EditorState.CHANGE_SCHEMATIC.register((id, schematic) -> {
			if(schematic != null) addContent(new EditorSchematicWidget(schematic));
		});
	}
}
