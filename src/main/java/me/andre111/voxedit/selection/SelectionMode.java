package me.andre111.voxedit.selection;

import net.minecraft.text.Text;

public enum SelectionMode {
	REPLACE,
	ADD,
	SUBTRACT;
	
	public Text asText() {
		return Text.translatable("voxedit.selection.mode."+name().toLowerCase());
	}
}
