package me.andre111.voxedit.client.gizmo;

import me.andre111.voxedit.client.EditorState;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

public abstract class Gizmo implements Iterable<Box> {
	public abstract Text getName();
	public abstract void addActions(GizmoActions actions);
	
	public void selected() {}
	public void deselected() {}
	
	public final void modified() {
		EditorState.MODIFY_GIZMO.invoker().accept(this);
	}
}
