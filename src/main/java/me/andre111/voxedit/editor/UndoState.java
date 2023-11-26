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
		for(int i=actions.size()-1; i>=0; i--) {
			actions.get(i).undo(world);
		}
	}
	public void redo(World world) {
		for(int i=0; i<actions.size(); i++) {
			actions.get(i).redo(world);
		}
	}
}
