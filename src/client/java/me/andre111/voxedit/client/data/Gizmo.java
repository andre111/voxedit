package me.andre111.voxedit.client.data;

import java.util.function.BiConsumer;

import me.andre111.voxedit.client.EditorState;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

public abstract class Gizmo implements Iterable<Box> {
	public abstract Text getName();
	public abstract void addActions(BiConsumer<Text, Runnable> actionConsumer);
	
	public final void modified() {
		EditorState.MODIFY_GIZMO.invoker().accept(this);
	}
}
