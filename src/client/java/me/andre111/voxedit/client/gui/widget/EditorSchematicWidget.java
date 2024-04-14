package me.andre111.voxedit.client.gui.widget;

import me.andre111.voxedit.client.renderer.SchematicRenderer;
import me.andre111.voxedit.state.Schematic;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EditorSchematicWidget extends ButtonWidget {
	private Identifier previewID;

	public EditorSchematicWidget(Schematic schematic) {
		super(0, 0, 96, 96, Text.empty(), (button) -> {
		}, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
		
		SchematicRenderer.getPreview(schematic, 90, 90).thenAccept(id -> previewID = id);
	}

	@Override
    public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
		if(previewID != null) {
			context.drawTexture(previewID, getX()+3, getY()+3, 0, 0, 90, 90);
		}
	}
}
