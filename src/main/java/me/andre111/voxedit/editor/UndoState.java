package me.andre111.voxedit.editor;

import java.util.List;

import me.andre111.voxedit.editor.action.EditAction;
import net.minecraft.world.World;

public class UndoState {
	private final List<EditAction> actions;
	
	public UndoState(List<EditAction> actions) {
		this.actions = List.copyOf(actions);
	}
	
	public void undo(World world) {
		for(EditAction action : actions) action.undo(world);
	}
	public void redo(World world) {
		for(EditAction action : actions) action.redo(world);
	}
}
