package me.andre111.voxedit.editor;

import java.util.List;

import me.andre111.voxedit.editor.action.EditAction;
import net.minecraft.world.World;

public class UndoState {
	private final List<EditAction> actions;
	
	public UndoState(List<EditAction> actions) {
		this.actions = List.copyOf(actions);
	}
	
	public int undo(World world) {
		int count = 0;
		for(int i=actions.size()-1; i>=0; i--) {
			count += actions.get(i).undo(world);
		}
		return count;
	}
	public int redo(World world) {
		int count = 0;
		for(int i=0; i<actions.size(); i++) {
			count += actions.get(i).redo(world);
		}
		return count;
	}
}
